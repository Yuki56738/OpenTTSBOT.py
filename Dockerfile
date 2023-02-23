FROM python:3.11.2

WORKDIR /usr/src/app

COPY requirements.txt ./

RUN pip install --no-cache-dir -r requirements.txt

RUN apt update

RUN apt install -y open-jtalk open-jtalk-mecab-naist-jdic ffmpeg
COPY . .
COPY .* .

CMD [ "python3", "main.py" ]