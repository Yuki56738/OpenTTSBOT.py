from pydub import AudioSegment

async def change_volume(new_volume: int, wavFileName: str):
    # Load the audio file
    audio = AudioSegment.from_file(wavFileName, format="wav")
    # Adjust the volume
    audio = audio + new_volume
    # Export the modified audio
    audio.export(wavFileName, format="wav")
