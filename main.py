import re
import sys
import os
from dotenv import load_dotenv
import discord
from voice_generator import create_WAV

load_dotenv()
TOKEN = os.environ.get("DISCORD_TOKEN")

intents = discord.Intents.all()
bot = discord.Bot(intents=intents)

read_channels = {}


@bot.event
async def on_ready():
    print(f"Logged in as: {bot.user}")


@bot.slash_command()
async def join(ctx):
    try:
        await ctx.author.voice.channel.connect()
    except:
        await ctx.respond("どこのVCにも参加していません!")
        return
    await ctx.respond("Connecting...")
    # read_channels.pop(ctx.author.guild.id, ctx.channel_id)
    read_channels[ctx.author.guild.id] = ctx.channel_id
    with open("greeting.txt", "r") as f:
        greeting = f.read()
    embed_msg = discord.Embed(title="Open読み上げBOTv3", colour=discord.Colour.magenta(), description=greeting)
    await ctx.channel.send(embed=embed_msg)

    text = "オープン読み上げボットです！"
    # WAVファイルを作成
    create_WAV(text)
    # WAVファイルをDiscordにインプット
    source = discord.FFmpegPCMAudio("output.wav")
    # 読み上げる
    # if yom_channel == message.channel.id:
    ctx.voice_client.play(source)

    print(read_channels)


@bot.slash_command()
async def leave(ctx):
    try:
        await ctx.voice_client.disconnect()
        await ctx.respond("Disconnecting...")
    except:
        await ctx.respond("どこのVCにも参加していません!")
    read_channels.pop(ctx.author.guild.id)


@bot.event
async def on_message(message):
    # for x in read_channels:
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
            # WAVファイルを作成
            create_WAV(text_alt)
            # WAVファイルをDiscordにインプット
            source = discord.FFmpegPCMAudio("output.wav")
            # 読み上げる
            # if yom_channel == message.channel.id:
            message.guild.voice_client.play(source)


@bot.event
async def on_voice_state_update(member, before, after):
    try:
        member.voice.channel.members
    except:
        await  member.guild.voice_client.disconnect()
        read_channels.pop(member.guild.id)


bot.run(TOKEN)
