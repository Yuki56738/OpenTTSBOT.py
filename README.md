無料で使える読み上げBOT.「Open読み上げBOT」  
招待はこちら:  
https://discord.com/api/oauth2/authorize?client_id=953590781703254026&permissions=4835056640&scope=bot%20applications.commands

問い合わせは以下のアドレスまで:  
yomi@risaton.net  
  
開発者向け:

※Ubuntu Linuxのみで動作検証しています。

```shell
git clone https://github.com/Yuki56738/Yuki-s-yomiage-BOT-v2.discord.git  
  
docker build -t tts .  
docker run --rm -it tts
```
  
従来の方法:  
1. 依存関係のインストール
```
sudo apt install ffmpeg pipenv  
#(Maybe required)  
pipenv --python 3  
pipenv install
```
2. トークンを記述
```
vi .env  
DISCORD_TOKEN="your-token"
```

3. 実行
```shell
pipenv run python main.py
```
