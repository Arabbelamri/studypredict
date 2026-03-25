import sys
sys.path.append("../automl_group13")

from automl.automl import AutoML
import joblib

automl = AutoML()
automl.fit("dataset_clean")

metrics = automl.eval()
print("Métriques :", metrics)
print("Modèle choisi :", automl.model.__class__.__name__)

joblib.dump(automl.model, "model.pkl")
joblib.dump(automl.feature_columns, "feature_columns.pkl")

print("model.pkl et feature_columns.pkl créés !")