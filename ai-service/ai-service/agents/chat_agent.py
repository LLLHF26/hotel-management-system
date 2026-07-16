"""智能客服对话 Agent —— 处理 FAQ、政策咨询等对话。"""

from __future__ import annotations

from langchain.agents import AgentExecutor, create_openai_tools_agent
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder

from core.llm import get_streaming_llm
from tools.knowledge_search import search_knowledge
from tools.room_query import query_room_types, query_available_rooms
from tools.order_query_tool import query_orders
from tools.customer_query import query_customer

SYSTEM_PROMPT = """你是酒店智能客服助手。你的职责是：
1. 根据知识库检索结果回答用户关于酒店政策、设施、周边等问题。
2. 如果用户询问房型或房间信息，调用 query_room_types 或 query_available_rooms 工具。
3. 如果用户询问订单相关问题（我的订单、住了几次、消费等），必须调用 query_orders 工具获取真实数据，不要问用户要订单号或会员ID。
4. 如果用户询问会员信息（积分、等级等），必须调用 query_customer 工具获取真实数据，不要问用户要会员ID。
5. 回答要友好、专业、简洁，使用中文。
6. 【重要】工具不传参数时会自动查询当前登录用户的数据，直接调用即可。

知识库检索结果：
{knowledge_context}
"""

PROMPT = ChatPromptTemplate.from_messages([
    ("system", SYSTEM_PROMPT),
    MessagesPlaceholder(variable_name="chat_history", optional=True),
    ("human", "{input}"),
    MessagesPlaceholder(variable_name="agent_scratchpad"),
])


def create_chat_agent() -> AgentExecutor:
    llm = get_streaming_llm()
    tools = [search_knowledge, query_room_types, query_available_rooms, query_orders, query_customer]
    agent = create_openai_tools_agent(llm, tools, PROMPT)
    return AgentExecutor(
        agent=agent,
        tools=tools,
        verbose=False,
        handle_parsing_errors=True,
    )
