import csv
import statistics
from prettytable import PrettyTable

# Nom du fichier à lire
filename = "scores_affinite_ens1ens2.csv"

# Initialisation
all_values = []
table = PrettyTable()
table.field_names = ["Nom", "Moyenne", "Variance", "Écart-type"]

# Lecture du fichier
with open(filename, newline='', encoding='utf-8') as csvfile:
    reader = csv.reader(csvfile, delimiter=';')
    headers = next(reader)  # Ignorer la ligne d'en-tête

    for row in reader:
        nom = row[0]
        try:
            valeurs = list(map(float, row[1:]))
        except ValueError:
            print(f"{nom} : données invalides")
            continue

        if len(valeurs) < 2:
            print(f"{nom} : pas assez de données pour calculer une variance")
            continue

        moyenne = statistics.mean(valeurs)
        variance = statistics.variance(valeurs)
        ecart_type = statistics.stdev(valeurs)

        # Ajouter à la table
        table.add_row([
            nom,
            f"{moyenne:.2f}",
            f"{variance:.2f}",
            f"{ecart_type:.2f}"
        ])

        # Ajouter aux valeurs globales
        all_values.extend(valeurs)

# Calcul global
moyenne_totale = statistics.mean(all_values)
mediane_totale = statistics.median(all_values)

# Affichage
print(table)
print("\nStatistiques globales sur toutes les valeurs :")
print(f"  Moyenne globale : {moyenne_totale:.2f}")
print(f"  Médiane globale : {mediane_totale:.2f}")
