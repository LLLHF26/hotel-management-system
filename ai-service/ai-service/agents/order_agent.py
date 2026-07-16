"""订单查询 Agent —— 自然语言解析意图并提取查询参数。"""

from __future__ import annotations

from langchain.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser

from core.llm import get_llm

SYSTEM_PROMPT = """你是酒店订单查询助手。用户会用自然语言描述查询需求，你需要从用户消息中提取查询参数。

请严格按照以下 JSON 格式输出（不要在 JSON 外添加其他文字）：

```json
{
  "parsed_intent": "查询历史订单",
  "parsed_params": {
    "start_date": "2026-04-01",
    "end_date": "2026-04-30",
    "status": "已完成"
  }
}
```

参数提取规则：
- "我的订单" / "所有订单" → params: {}
- "上个月" / "上月" → start_date=上月第一天, end_date=上月最后一天
- "本月" → start_date=本月第一天, end_date=今天
- "最近N天" → start_date=N天前, end_date=今天
- "今天" → start_date=今天, end_date=今天
- "即将" / "未来" → start_date=今天, end_date 不设（或设为年底）
- "待支付" → status: "待支付"
- "已支付" → status: "已支付"
- "已入住" / "入住中" → status: "已入住"
- "已完成" → status: "已完成"
- "已取消" → status: "已取消"
- "最近一笔" / "最新" → 按时间倒序取最新
- 所有日期使用 YYYY-MM-DD 格式
- parsed_intent 用中文简述识别到的查询意图
"""

PROMPT = ChatPromptTemplate.from_messages([
    ("system", SYSTEM_PROMPT),
    ("human", "{input}"),
])


def create_order_agent():
    """创建订单查询参数提取链：LLM → 字符串输出。"""
    llm = get_llm()
    return PROMPT | llm | StrOutputParser()
