"""异常告警接口。"""

from __future__ import annotations

import logging
from datetime import date, timedelta

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from api.deps import get_current_user, get_db
from core.auth import ROLE_ADMIN, ForbiddenError
from core.exceptions import NotFoundError
from models.alert import Alert, AlertRule
from schemas.alert import (
    AlertRuleCreate,
    AlertRuleUpdate,
    AlertRuleVO,
    AlertStatusResponse,
    AlertVO,
)
from schemas.common import Result

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/ai/alert", tags=["异常告警"])


def _require_admin(user: dict) -> None:
    if user.get("role") != ROLE_ADMIN:
        raise ForbiddenError("仅管理员可操作告警规则")


@router.get("/status", summary="当前告警状态", response_model=Result[AlertStatusResponse])
async def alert_status(
    db: Session = Depends(get_db),
    user: dict = Depends(get_current_user),
):
    alerts = db.query(Alert).order_by(Alert.trigger_time.desc()).limit(50).all()
    unread = db.query(Alert).filter(Alert.is_read == False).count()
    return Result.ok(
        AlertStatusResponse(
            alerts=[AlertVO.model_validate(a) for a in alerts],
            unread_count=unread,
        )
    )


@router.put("/{alert_id}/read", summary="标记告警已读", response_model=Result[None])
async def mark_read(alert_id: int, db: Session = Depends(get_db)):
    alert = db.get(Alert, alert_id)
    if not alert:
        raise NotFoundError(f"告警 {alert_id} 不存在")
    alert.is_read = True
    db.commit()
    return Result.ok(msg="已标记为已读")


@router.put("/read-all", summary="标记全部已读", response_model=Result[None])
async def mark_all_read(db: Session = Depends(get_db)):
    db.query(Alert).filter(Alert.is_read == False).update({"is_read": True})
    db.commit()
    return Result.ok(msg="已全部标记为已读")


@router.post("/rule", summary="配置告警规则", response_model=Result[dict])
async def create_rule(
    body: AlertRuleCreate,
    db: Session = Depends(get_db),
    user: dict = Depends(get_current_user),
):
    _require_admin(user)
    rule = AlertRule(**body.model_dump())
    db.add(rule)
    db.commit()
    db.refresh(rule)
    return Result.ok({"id": rule.id}, msg="规则已保存")


@router.get("/rules", summary="告警规则列表", response_model=Result[list[AlertRuleVO]])
async def list_rules(
    db: Session = Depends(get_db),
    user: dict = Depends(get_current_user),
):
    _require_admin(user)
    rules = db.query(AlertRule).order_by(AlertRule.create_time.desc()).all()
    return Result.ok([AlertRuleVO.model_validate(r) for r in rules])


@router.put("/rule/{rule_id}", summary="更新告警规则", response_model=Result[None])
async def update_rule(
    rule_id: int,
    body: AlertRuleUpdate,
    db: Session = Depends(get_db),
    user: dict = Depends(get_current_user),
):
    _require_admin(user)
    rule = db.get(AlertRule, rule_id)
    if not rule:
        raise NotFoundError(f"告警规则 {rule_id} 不存在")
    for key, val in body.model_dump(exclude_unset=True).items():
        setattr(rule, key, val)
    db.commit()
    return Result.ok(msg="规则已更新")


@router.delete("/rule/{rule_id}", summary="删除告警规则", response_model=Result[None])
async def delete_rule(
    rule_id: int,
    db: Session = Depends(get_db),
    user: dict = Depends(get_current_user),
):
    _require_admin(user)
    rule = db.get(AlertRule, rule_id)
    if not rule:
        raise NotFoundError(f"告警规则 {rule_id} 不存在")
    db.delete(rule)
    db.commit()
    return Result.ok(msg="规则已删除")


@router.post("/check", summary="手动触发告警检查", response_model=Result[dict])
async def manual_check(
    db: Session = Depends(get_db),
    user: dict = Depends(get_current_user),
):
    """手动触发一次告警检查，扫描告警规则并生成告警记录。"""
    rules = db.query(AlertRule).filter(AlertRule.is_enabled == True).all()
    if not rules:
        return Result.ok({"triggered": 0}, msg="无启用的告警规则")

    token = user.get("token", "")
    triggered = 0
    for rule in rules:
        try:
            result = _evaluate_rule(rule, token)
            if result:
                alert = Alert(
                    type=rule.type,
                    level=result.get("level", "WARNING"),
                    content=result.get("content", ""),
                )
                db.add(alert)
                triggered += 1
        except Exception:
            logger.exception("告警规则 %s 评估失败", rule.type)

    if triggered > 0:
        db.commit()
    return Result.ok({"triggered": triggered}, msg=f"触发 {triggered} 条告警")


def run_alert_check() -> int:
    """后台调用的告警检查函数（不使用 Depends，自己创建 session）。"""
    from models.database import SessionLocal

    db = SessionLocal()
    try:
        rules = db.query(AlertRule).filter(AlertRule.is_enabled == True).all()
        if not rules:
            return 0
        triggered = 0
        for rule in rules:
            try:
                result = _evaluate_rule(rule, "")
                if result:
                    alert = Alert(
                        type=rule.type,
                        level=result.get("level", "WARNING"),
                        content=result.get("content", ""),
                    )
                    db.add(alert)
                    triggered += 1
            except Exception:
                logger.exception("后台告警检查失败: %s", rule.type)
        if triggered > 0:
            db.commit()
        return triggered
    finally:
        db.close()


def _evaluate_rule(rule: AlertRule, token: str) -> dict | None:
    """评估单条告警规则，返回告警内容或 None。"""

    if rule.type == "满房预警":
        return _check_occupancy(rule, token)

    if rule.type == "价格异常":
        return _check_price_anomaly(rule, token)

    if rule.type == "异常退单":
        return _check_refund_anomaly(rule, token)

    return None


def _call_direct(url: str, *, token: str, params: dict | None = None) -> dict:
    """绕过网关直接调用后端微服务。"""
    import httpx

    headers = {"Accept": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    resp = httpx.get(url, params=params, headers=headers, timeout=30.0)
    resp.raise_for_status()
    return resp.json()


def _check_occupancy(rule: AlertRule, token: str) -> dict | None:
    try:
        today = date.today()
        start = (today - timedelta(days=7)).isoformat()
        resp = _call_direct("http://localhost:8084/api/finance/analysis/occupancy-by-type",
                            token=token or "",
                            params={"startDate": start, "endDate": today.isoformat()})
        data = resp.get("data", [])
        if isinstance(data, list):
            for item in data:
                raw = str(item.get("occupancyRate", item.get("occupancy_rate", "0")))
                rate = float(raw.replace("%", "")) / 100.0
                if rate >= rule.threshold:
                    return {
                        "level": "WARNING",
                        "content": f"{item.get('roomTypeName', '未知房型')} 入住率 {rate:.0%}，超过阈值 {rule.threshold:.0%}",
                    }
    except Exception:
        logger.exception("满房预警检查失败")
    return None


def _check_price_anomaly(rule: AlertRule, token: str) -> dict | None:
    """检测价格异常：从 finance-service 获取日营收趋势，比较价格波动。"""
    try:
        today = date.today()
        start = (today - timedelta(days=7)).isoformat()
        resp = _call_direct("http://localhost:8084/api/finance/revenue/daily",
                            token=token or "",
                            params={"startDate": start, "endDate": today.isoformat()})
        data = resp.get("data", {})
        if not isinstance(data, dict):
            return None
        revenues = data.get("totalRevenue", [])
        if not revenues or len(revenues) < 3:
            return None

        daily_totals = [float(r) for r in revenues]
        if sum(daily_totals) <= 0:
            return None

        recent_avg = sum(daily_totals[-3:]) / 3
        early_avg = sum(daily_totals[:3]) / 3
        if early_avg <= 0 and recent_avg <= 0:
            return None
        if early_avg <= 0:
            change = 1.0  # 从零到有，视为 100% 变化
        else:
            change = abs(recent_avg - early_avg) / early_avg
        if change >= rule.threshold:
            direction = "上涨" if recent_avg > early_avg else "下降"
            return {
                "level": "WARNING",
                "content": f"近7天日均营收 {direction} {change:.1%}（从 {early_avg:.2f} 到 {recent_avg:.2f}），超过阈值 {rule.threshold:.0%}",
            }
    except Exception:
        logger.exception("价格异常检查失败")
    return None


def _check_refund_anomaly(rule: AlertRule, token: str) -> dict | None:
    """检测异常退单：统计近期退款率，超过阈值则告警。"""
    try:
        today = date.today()
        start = (today - timedelta(days=7)).isoformat()

        resp = _call_direct("http://localhost:8083/api/order/list/byTime",
                            token=token or "",
                            params={"startDate": start, "endDate": today.isoformat()})
        all_orders = resp.get("data", [])
        if not isinstance(all_orders, list):
            return None

        refund_orders = [o for o in all_orders if o.get("status") == "已退款"]
        refund_count = len(refund_orders)
        total_count = len(all_orders)

        if total_count >= 2:
            refund_rate = refund_count / total_count
            if refund_rate >= rule.threshold:
                return {
                    "level": "WARNING",
                    "content": f"近7天退款率 {refund_rate:.1%}（{refund_count}/{total_count}），超过阈值 {rule.threshold:.0%}",
                }
    except Exception:
        logger.exception("异常退单检查失败")
    return None
