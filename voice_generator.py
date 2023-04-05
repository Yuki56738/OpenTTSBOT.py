import os.path
import pathlib
import subprocess
import sys


def create_WAV(inputText):
    # message.contentをテキストファイルに書き込み
    # input_file = 'input.txt'
    # if inFile is None:
    input_file = 'input.txt'
    # input_file = inFile

    # 読み上げる文章をファイルに書き出し
    with open(input_file, 'w') as file:
        file.write(inputText)
    # open_jtalkのコマンドの用意
    # command = '/usr/bin/open_jtalk -x {x} -m {m} -r {r} -ow {ow} {input_file}'
    command = 'open_jtalk -x {x} -m {m} -r {r} -ow {ow} {input_file}'


    # 辞書のPath
    x = '/var/lib/mecab/dic/open-jtalk/naist-jdic'
    if not os.path.exists(x):
        x = '/opt/local/lib/open_jtalk/dic'
    # x = '/opt/homebrew/lib/mecab/dic/ipadic'
    # x = '/usr/local/lib/mecab/dic/ipadic'
    # x = '/usr/local/lib/mecab/dic/ipadic'

    # ボイスファイルのPath
    # m = '/usr/share/hts-voice/nitech-jp-atr503-m001/nitech_jp_atr503_m001.htsvoice'
    m = './mei_normal.htsvoice'

    # 発声のスピード
    r = '0.7'

    # 出力ファイル名
    ow = 'output.wav'

    # 実行コマンドに変数を代入
    args = {'x': x, 'm': m, 'r': r, 'ow': ow, 'input_file': input_file}
 
    # コマンド実行の用意
    cmd = command.format(**args)
    cmd2 = cmd.split(" ")
    print(cmd)

    # open_jtalkを実行
    subprocess.run(cmd2)

    # 終了
    return True


if __name__ == '__main__':
    create_WAV('テスト')