# Rag-qdrant
# Ollama + Qdrant Setup Guide

This guide helps you set up Ollama and Qdrant on your local machine, including downloading embedding/chat models and running the Qdrant vector database using Docker.

## Prerequisites

- A local machine (Windows, macOS, or Linux)
- Internet connection
- [Ollama](https://ollama.com/) installed
- [Docker](https://www.docker.com/) installed

---

## 1. Install Ollama

Download and install Ollama from the official website:

🔗 [https://ollama.com/](https://ollama.com/)

Follow the installation instructions specific to your OS.

---

## 2. Pull Embedding Models

You can pull the embedding models you'd like to use from the Ollama model library:

```bash
ollama pull <model-name>
```
You can also pull free GGUF models hosted on Hugging Face:
```bash
ollama pull hf.co/<username>/<model-repository>
Example:
ollama pull mxbai-embed-large-v1
```
## 3. Install Chat Model
To use a chat model locally:
```bash
ollama pull <model-name>
```
Replace \<model-name\> with the desired chat model (e.g., llama2, phi).

## 4. Set Up Qdrant with Docker
Qdrant is a high-performance vector similarity search engine.

### a. Install Docker
If you haven't already, install Docker from:

🔗 https://www.docker.com/products/docker-desktop

### b. Pull the Qdrant Docker Image
Use the following command to pull the latest Qdrant image:
```bash
docker pull qdrant/qdrant
```
### c. Run Qdrant
You can start the Qdrant server using:
```bash
docker run -p 6333:6333 -v $(pwd)/qdrant_storage:/qdrant/storage qdrant/qdrant
```

This runs Qdrant on port 6333 and stores data in a local directory named qdrant_storage.

### Notes
Ensure Docker is running before executing Qdrant commands.

You can explore the Qdrant API at http://localhost:6333.

