import openai
import os
import json
import sys
import asyncio
from tqdm import tqdm
from typing import List, Dict

gemini_api_key = os.getenv("GEMINI_API_KEY")

client = openai.OpenAI(
    base_url="https://generativelanguage.googleapis.com/v1beta/openai/",
    api_key=gemini_api_key,
)


def chat(messages, response_format=None):
    response = client.chat.completions.create(
        model="gemini-2.5-flash",
        messages=messages,
        response_format=response_format,
    )
    return response.choices[0].message.content

