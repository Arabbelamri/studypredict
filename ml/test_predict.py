import joblib
import pandas as pd

model = joblib.load("model.pkl")
feature_columns = joblib.load("feature_columns.pkl")

# Mapping : nom réel → numéro de colonne
# L'ordre correspond à celui dans prepare_data.py
COLUMN_MAP = {
    "Hours_Studied": "0",
    "Attendance": "1",
    "Sleep_Hours": "2",
    "Previous_Scores": "3",
    "Extracurricular_Activities": "4",
    "Tutoring_Sessions": "5",
    "Physical_Activity": "6"
}

def predict_score(data: dict) -> dict:
    # Convertir les noms en numéros
    mapped = {COLUMN_MAP[k]: v for k, v in data.items()}
    
    df = pd.DataFrame([mapped])
    df = df.reindex(columns=feature_columns, fill_value=0)
    
    prediction = model.predict(df)
    score = float(prediction[0])
    
    if score >= 90: grade = "A"
    elif score >= 75: grade = "B"
    elif score >= 60: grade = "C"
    else: grade = "D"
    
    return {"predicted_score": round(score, 2), "grade": grade}


# === TESTS ===
print("=== TEST PLUSIEURS PROFILS ===\n")

profils = [
    ("Etudiant sérieux", {
        "Hours_Studied": 40, "Attendance": 95, "Sleep_Hours": 8,
        "Previous_Scores": 85, "Extracurricular_Activities": 1,
        "Tutoring_Sessions": 3, "Physical_Activity": 4
    }),
    ("Etudiant moyen", {
        "Hours_Studied": 15, "Attendance": 70, "Sleep_Hours": 6,
        "Previous_Scores": 60, "Extracurricular_Activities": 0,
        "Tutoring_Sessions": 1, "Physical_Activity": 2
    }),
    ("Etudiant en difficulté", {
        "Hours_Studied": 10, "Attendance": 0, "Sleep_Hours": 5,
        "Previous_Scores": 30, "Extracurricular_Activities": 0,
        "Tutoring_Sessions": 0, "Physical_Activity": 1
    }),
]

for nom, data in profils:
    result = predict_score(data)
    print(f"{nom:30s} → Score: {result['predicted_score']:6.2f} | Grade: {result['grade']}")