package xyz.wolegelei.nepu_fams.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.*;
import xyz.wolegelei.nepu_fams.common.Result;

import java.util.Map;

/**
 * AI 智能助手聊天接口
 * 使用 Sa-Token 认证，登录用户即可访问
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final ChatModel chatModel;
    private final FamsAgentTools famsAgentTools;

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        if (message == null || message.isBlank()) {
            return Result.fail("消息不能为空");
        }

        log.info("AI 聊天请求: {}", message);

        try {
            String response = ChatClient.create(chatModel)
                    .prompt()
                    .system("你是 NEPU-FAMS 固定资产管理系统的智能助手。请优先使用可用工具查询真实业务数据，再用简洁中文回答用户。")
                    .user(message)
                    .tools(famsAgentTools)
                    .call()
                    .content();

            return Result.success(response);
        } catch (Exception e) {
            log.error("AI 聊天处理失败", e);
            return Result.fail("AI 服务暂时不可用: " + e.getMessage());
        }
    }
}
