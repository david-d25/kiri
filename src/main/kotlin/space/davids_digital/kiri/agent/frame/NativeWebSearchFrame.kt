package space.davids_digital.kiri.agent.frame

import space.davids_digital.kiri.llm.ChatCompletionWebSearch

class NativeWebSearchFrame(
    val webSearch: ChatCompletionWebSearch
) : Frame()