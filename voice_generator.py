import os
import requests
#from dotenv import load_dotenvf

#load_dotenv()
to_post_url = os.environ.get("VVOX_URL")


def create_WAV(text: str):
    global to_post_url
    params = {
        'speaker': '1',
        'text': text,
    }

    response = requests.post(f'http://{to_post_url}/audio_query', params=params)

    print(response.status_code)

    headers = {
        'Content-Type': 'application/json',
    }

    params = {
        'speaker': '1',
    }
    data = response.content

    response1 = requests.post(f'http://{to_post_url}/synthesis', params=params, headers=headers, data=data)

    with open('output.wav', 'wb') as file:
        file.write(response1.content)


if __name__ == "__main__":
    create_WAV("こんにちは、世界に！")
