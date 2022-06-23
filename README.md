無料で使える読み上げBOT.  
招待はこちら:  
https://discord.com/api/oauth2/authorize?client_id=953590781703254026&permissions=3152896&scope=bot%20applications.commands  

問い合わせは以下のアドレスまで:  
engineer@risaton.net  
  
開発者向け:  
  
依存関係:  
```shell
sudo apt install open-jtalk open-jtalk-mecab-naist-jdic hts-voice-nitech-jp-atr503-m001  
```  
or  
```shell
brew install open-jtalk  
brew install mecab
brew install mecab-ipadic
```
1. 依存関係のインストール
```
brew install open-jtalk  
brew install mecab
brew install mecab-ipadic
```
2. トークンを記述
```
vi .env
```

3. 実行
```shell
java -jar OpenYomiageBot-3.jar
```