import functools
import re
import sys
import os
import wave

from dotenv import load_dotenv
import discord
from discord import *
from voice_generator import create_WAV

# load_dotenv()
load_dotenv('.envDev')
TOKEN = os.environ.get("DISCORD_TOKEN")

intents = discord.Intents.all()
bot = discord.Bot(intents=intents)

read_channels = {}
q = asyncio.Queue()
# loop = asyncio.new_event_loop()

import asyncio

class MyGuildAsyncQueueClass:
    queues = {}

    @classmethod
    def add_to_queue(cls, guild_id, value):
        if guild_id not in cls.queues:
            cls.queues[guild_id] = asyncio.Queue()
        asyncio.create_task(cls.queues[guild_id].put(value))

    @classmethod
    async def get_from_queue(cls, guild_id):
        try:
            queue = cls.queues[guild_id]
        except KeyError:
            return None
        try:
            return await asyncio.wait_for(queue.get(), timeout=1.0)
        except asyncio.TimeoutError:
            return None

    @classmethod
    def is_queue_empty(cls, guild_id):
        try:
            queue = cls.queues[guild_id]
        except KeyError:
            return True
        return queue.empty()
        # self.q = asyncio.Queue()

@bot.event
async def on_ready():
    print(f"Logged in as: {bot.user}")


@bot.slash_command(name="join", description="VCに接続.")
async def join(ctx: ApplicationContext):
    try:
        await ctx.author.voice.channel.connect()
    except:
        # await ctx.respond("どこのVCにも参加していません!")
        # return
        pass
    await ctx.respond("Connecting...")
    # read_channels.pop(ctx.author.guild.id, ctx.channel_id)
    read_channels[ctx.author.guild.id] = ctx.channel_id
    with open("greeting.txt", "r") as f:
        greeting = f.read()
    embed_msg = discord.Embed(title="Open読み上げBOTv3.1", colour=discord.Colour.magenta(), description=greeting)
    await ctx.channel.send(embed=embed_msg)

    text = "読み上げです！"
    # WAVファイルを作成
    create_WAV(text)
    # WAVファイルをDiscordにインプット
    source = discord.FFmpegPCMAudio("output.wav")
    # 読み上げる
    # if yom_channel == message.channel.id:
    ctx.voice_client.play(source)

    print(read_channels)
    # queues1 = queues()
    # queues1.guildq[str(ctx.guild_id)] =
    # queue_1 = queue()
    # MyGuildAsyncQueueClass
    vc = ctx.voice_client

    loop = asyncio.get_event_loop()
    loop.create_task(play(vc, loop))


@bot.slash_command(name="leave", description="VCから切断.")
async def leave(ctx: ApplicationContext):
    await ctx.voice_client.disconnect()
    await ctx.respond("Disconnecting...")
    read_channels.pop(ctx.author.guild.id)


@bot.event
async def on_message(message: Message):
    if message.content.startswith(".debug"):
        print(read_channels)
        for x in bot.guilds:
            print(x)
    if message.author.bot:
        return
    if read_channels.get(message.author.guild.id) == message.channel.id:
        msg = message.content
        # URLを読み上げない
        if message.content.startswith("http://") or message.content.startswith("https://"):
            msg = "URL省略"
        # 50文字までしか読み上げない
        if len(message.content) <= 50:
            # メンションを読み上げない
            pattern = r'<@!'
            text = re.sub(pattern, '', msg)  # 置換処理
            pattern = r'[0-9]+>'
            # return re.sub(pattern,'',msg) # 置換処理
            text_alt = re.sub(pattern, '', msg)
            text_alt = re.sub("w+", "わら", text_alt)
            text_alt = re.sub("W+", "わら", text_alt)
            text_alt = re.sub("ｗ+", "わら", text_alt)
            text_alt = re.sub("\n", "", text_alt)

            # message.guild.voice_client.is_playing():

            # WAVファイルを作成
            # create_WAV(text_alt)
            # with wave.open('output.wav', 'rb') as wr:
            #     fr = wr.getframerate()
            #     fn = wr.getnframes()
            # print('wav再生時間:', 1.0 * fn / fr)
            vc = message.guild.voice_client
            global q
            await q.put(text_alt)

            # global loop


            # if not vc.is_playing():
                # loop.call_soon_threadsafe(await play(vc, loop))
            # await q.join()
            # loop.create_task(play(vc, loop))
            # await play(vc, loop)
            # loop.call_soon_threadsafe(await play_wav(vc))
            # loop.run_in_executor(None, play_wav(message.guild.voice_client))
            # loop.run_in_executor(None, await play_wav(message.guild.voice_client))
            # loop.create_task(play_wav(message.guild.voice_client))
            # WAVファイルをDiscordにインプット
            # source = discord.FFmpegPCMAudio("output.wav")
            # # 読み上げる
            # # if yom_channel == message.channel.id:
            # await message.guild.voice_client.play(source)
            # message.guild.voice_client: VoiceClient
            # asyncio.run("output.wav")


async def play(voice_client: VoiceClient, loop):
    global q
    while True:
        text = await q.get()
        create_WAV(text)
        source = discord.FFmpegPCMAudio("output.wav")
        voice_client.play(source)
        while voice_client.is_playing():
            await asyncio.sleep(0.1)
        q.task_done()

        # q.task_done()

        # else:

        # if q.empty():
        #     q.task_done()
        #     break
        # break


@bot.event
async def on_voice_state_update(member: Member, before: VoiceState, after: VoiceState):
    try:
        read_channels[member.guild.id]
    except:
        return
    # if read_channels[member.guild.id] is not None:
    if after.channel is None:
        if len(before.channel.members) == 1:
            await member.guild.voice_client.disconnect()
            read_channels.pop(member.guild.id)
    else:
        if after.channel.id != before.channel.id:
            if len(before.channel.members) == 1:
                await member.guild.voice_client.disconnect()
                read_channels.pop(member.guild.id)
    # try:
    #     member.voice.channel.members
    # except:
    #     await member.guild.voice_client.disconnect()
    #     read_channels.pop(member.guild.id)


bot.run(TOKEN)
