import asyncio
import signal
import sys
from telethon import TelegramClient, events
import logging

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger()

# 替换为你自己的 API ID 和 API hash
api_id = 25966540  # 替换为整数形式的 API ID
api_hash = '8f9f1ead1641d2ba5630da90ead3c1a2'

# 登录信息
phone_number = '+85366152542'  # 替换为你的手机号码

# 初始化 TelegramClient
client = TelegramClient('session_name', api_id, api_hash)

# 要忽略的特定机器人的用户名
ignore_bot_usernames = ['chadanzs_bot', 'another_bot']  # 替换为你要忽略的机器人的用户名

# 存储当前群组 ID
group_ids = []

async def update_group_ids():
    """更新群组 ID 列表"""
    global group_ids
    try:
        dialogs = await client.get_dialogs()
        group_ids = [dialog.id for dialog in dialogs if dialog.is_group]
        logger.info(f"更新后的群组 ID 列表: {group_ids}")
    except Exception as e:
        logger.error(f"更新群组 ID 时出错: {e}")

@client.on(events.NewMessage)  # 监听所有新消息事件
async def handler(event):
    """处理新消息事件"""
    try:
        chat_id = event.chat_id
        if chat_id not in group_ids:
            logger.info(f"忽略非群组消息，来自 chat_id: {chat_id}")
            return

        message = event.message
        sender = await message.get_sender()

        logger.info(f"接收到消息: '{message.text}' 来自: {sender.id}, 发送者名称: {sender.first_name}, 是否机器人: {sender.bot}")

        # 判断消息是否来自机器人且包含图片和文字说明
        if sender.bot and message.photo and message.text:
            logger.info(f"接收到来自机器人的图文消息: '{message.text}' 来自 chat_id: {chat_id}")
            try:
                # 转发消息到当前群组
                await client.forward_messages(chat_id, message)
                logger.info(f"消息已转发: {message.text}")
            except Exception as e:
                logger.error(f"转发消息时出错: {e}")
        else:
            logger.info(f"消息不符合条件，忽略")

    except Exception as e:
        logger.error(f"处理消息时出错: {e}")

@client.on(events.ChatAction)  # 监听用户加入或退出群组
async def chat_action_handler(event):
    """处理群组动作事件"""
    try:
        if event.user_added or event.user_joined:  # 用户加入事件
            logger.info(f"用户加入了群组: {event.chat_id}")
            await update_group_ids()
        elif event.user_kicked or event.user_left:  # 用户离开事件
            logger.info(f"用户离开了群组: {event.chat_id}")
            await update_group_ids()
    except Exception as e:
        logger.error(f"处理群组动作时出错: {e}")

async def main():
    """主函数"""
    try:
        await client.connect()
        if not await client.is_user_authorized():
            logger.info("用户未授权，需要登录")
            await client.send_code_request(phone_number)
            code = input('输入验证码: ')
            try:
                await client.sign_in(phone_number, code)
            except telethon.errors.SessionPasswordNeededError:
                logger.info("两步验证已启用，需要输入密码")
                password = input("输入两步验证密码: ")
                await client.sign_in(password=password)
            logger.info("登录成功")
        await update_group_ids()
        await client.run_until_disconnected()
    except Exception as e:
        logger.error(f"程序启动或运行时出错: {e}")
    finally:
        await client.disconnect()
        logger.info("已断开连接")

def signal_handler(sig, frame):
    """处理 Ctrl+C 信号"""
    logger.info("程序中断，正在退出...")
    asyncio.ensure_future(client.disconnect())
    sys.exit(0)

if __name__ == '__main__':
    signal.signal(signal.SIGINT, signal_handler)  # 处理 Ctrl+C
    loop = asyncio.get_event_loop()
    try:
        loop.run_until_complete(main())
    except KeyboardInterrupt:
        logger.info("程序被用户中断")
    except Exception as e:
        logger.error(f"程序执行期间发生未捕获的错误: {e}")
    finally:
        loop.close()
