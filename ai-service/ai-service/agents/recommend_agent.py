"""房型推荐 Agent —— 根据用户偏好和条件推荐合适房型。"""

from __future__ import annotations

from langchain.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser

from core.llm import get_llm

SYSTEM_PROMPT = """你是酒店房型推荐助手。用户会提供入住条件和可用房型数据，你需要根据条件筛选并排序推荐。

请严格按照以下 JSON 格式输出推荐结果（不要在 JSON 外添加其他文字）：

```json
[
  {{
    "room_type_name": "豪华大床房",
    "score": 0.92,
    "reason": "价格在预算范围内，大床适合2人入住，当前有5间空房"
  }}
]
```

推荐原则：
- 优先匹配人数和预算要求（价格在 budget_min 到 budget_max 之间得分更高）
- 考虑用户偏好描述（如高层、安静、景观等），在 reason 中体现
- score 范围 0-1，按匹配度从高到低排序
- 只推荐 available_count > 0 的房型
- 如果没有匹配的房型，输出空数组 []
"""

PROMPT = ChatPromptTemplate.from_messages([
    ("system", SYSTEM_PROMPT),
    ("human", "{input}"),
])


def create_recommend_agent():
    """创建推荐链：LLM → 字符串输出。"""
    llm = get_llm()
    return PROMPT | llm | StrOutputParser()
