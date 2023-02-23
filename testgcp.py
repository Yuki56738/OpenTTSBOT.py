import oauth2client
from google.cloud import texttospeech
import google.oauth2.credentials
import google_auth_oauthlib.flow
import os

GCP_SCOPES = [ 'https://www.googleapis.com/auth/cloud-platform' ]

flow = google_auth_oauthlib.flow
# flow = oauth2client.client.OAuth2WebServerFlow(
#         client_id="728514411829-86t13hs5g8uqrhhs1br1npqeoohkpmmg.apps.googleusercontent.com",
#         client_secret="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",
#         scope='https://www.googleapis.com/auth/cloud-platform',
#         user_agent=USER_AGENT,
#         oauth_displayname=OAUTH_DISPLAY_NAME)
# )

from dotenv import load_dotenv

load_dotenv('.envDev')

gcpclient = texttospeech.TextToSpeechClient()

synthesis_input = texttospeech.SynthesisInput(text="Hello, world!")
voice = texttospeech.VoiceSelectionParams(
    language_code="ja-JP",
    name='ja-JP-Neural2-B'
)
audio_config = texttospeech.AudioConfig(
    audio_encoding=texttospeech.AudioEncoding.MP3
)
response = gcpclient.synthesize_speech(
    input=synthesis_input, voice=voice, audio_config=audio_config
)

with open("output.mp3", "wb") as out:
    out.write(response.audio_content)
