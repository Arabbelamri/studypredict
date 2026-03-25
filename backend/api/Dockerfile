FROM python:3.11-slim

WORKDIR /app

COPY backend/api/requirements.txt ./requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

COPY backend/api/app ./app
COPY ml/model.pkl ./ml/model.pkl
COPY ml/feature_columns.pkl ./ml/feature_columns.pkl

EXPOSE 8080

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8080"]
