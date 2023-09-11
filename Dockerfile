FROM ubuntu:latest

# Set environment variable for Discord bot token
ENV DISCORD_TOKEN=$DISCORD_TOKEN

# Install dependencies
RUN apt-get update && \
    apt-get install -y python3 python3-pip ffmpeg && \
    apt install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/*

# Copy the Discord bot files to the container
COPY . /app
# Change working directory to /app
WORKDIR /app

# Install pipenv via pip3
RUN pip3 install pipenv

# Set Python version to current Linux'
RUN pipenv --python 3

# Install Python dependencies
RUN pipenv install

# Set the working directory
WORKDIR /app

# Set the DISCORD_TOKEN environment variable before running the bot
CMD ["bash", "-c", "export DISCORD_TOKEN=$DISCORD_TOKEN && pipenv run python3 main.py"]
