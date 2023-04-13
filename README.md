無料で使える読み上げBOT.「Open読み上げBOT」  
招待はこちら:  
https://discord.com/api/oauth2/authorize?client_id=953590781703254026&permissions=4835056640&scope=bot%20applications.commands

問い合わせは以下のアドレスまで:  
yomi@risaton.net  
  
開発者向け:

※Ubuntu Linuxのみで動作検証しています。

ダウンロード:  
```shell
git clone https://github.com/Yuki56738/Yuki-s-yomiage-BOT-v2.discord.git
```

1. 依存関係のインストール
```
sudo apt install open-jtalk open-jtalk-mecab-naist-jdic hts-voice-nitech-jp-atr503-m001 ffmpeg pipenv  
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
