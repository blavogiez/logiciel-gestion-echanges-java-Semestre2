import csv
from datetime import date, datetime
from dateutil.relativedelta import relativedelta
from prettytable import PrettyTable

# Définition de la classe Personne
class Personne:
    def __init__(self, nom, hobbies, gender, pair_gender, naiss):
        self.nom = nom
        self.hobbies = [h.strip() for h in hobbies.split(",")]
        self.gender = gender
        self.pair_gender = pair_gender
        self.naiss = datetime.strptime(naiss, "%d/%m/%y").date()

# Fonction pour calculer le nombre de hobbies en commun
def nombre_hobbies_en_commun(hote, visiteur):
    return len(set(hote.hobbies) & set(visiteur.hobbies))

# Fonction pour calculer le score d'affinité
def score_affinite_1(hote, visiteur):
    score = 10
    date_aujourdhui = date.today()

    # Calcul des âges
    hote_age = relativedelta(date_aujourdhui, hote.naiss).years
    visiteur_age = relativedelta(date_aujourdhui, visiteur.naiss).years
    age_moyen = (hote_age + visiteur_age) / 2

    # Calcul de la différence d'âge en mois
    diff_mois = abs((hote.naiss.year - visiteur.naiss.year) * 12 + (hote.naiss.month - visiteur.naiss.month))
    #print(hote.nom + " et " + visiteur.nom + " ont comme diff en mois : " + str(diff_mois))
    # Ajustement du score en fonction de la différence d'âge
    if diff_mois <= 18:
        score += (diff_mois / 12) * (0.9 ** age_moyen)
        score *= 0.9
    else:
        score += ((diff_mois / 12) * (0.9 ** age_moyen))
        score *= 1.5

    # Ajustement du score en fonction des préférences de genre
    if (len(visiteur.pair_gender)!=0) :
        if hote.gender != visiteur.pair_gender:
            score *= 1.5
    if (len(hote.pair_gender)!=0):
        if visiteur.gender != hote.pair_gender:
            score *= 1.5

    # Ajustement du score en fonction des hobbies en commun
    N = nombre_hobbies_en_commun(hote, visiteur)
    score *= 0.85 ** N

    return round(score, 2)

# Initialisation des données des hôtes
hotes = [
    Personne("Alix", "football, trees, cats, tea", "male", "", "19/06/11"),
    Personne("Brune", "basket, music, kpop, dogs", "female", "female", "20/09/12"),
    Personne("Charlie", "running, computer, dogs", "other", "other", "05/12/10"),
    Personne("Diego", "swimming, running, hiking", "other", "male", "24/04/11")
]

# Initialisation des données des visiteurs
visiteurs = [
    Personne("William", "football, handball, computer", "male", "male", "26/09/13"),
    Personne("Xavier", "handball, running, cats", "male", "male", "27/09/12"),
    Personne("Yvan", "football, trees, dogs, coffee", "other", "other", "12/09/13"),
    Personne("Zorro", "running, hiking, music", "female", "", "10/12/12")
]

# Préparation des en-têtes du fichier CSV
header = [''] + [visiteur.nom for visiteur in visiteurs]

# Calcul des scores et préparation des lignes du fichier CSV
rows = []
for hote in hotes:
    row = [hote.nom]
    for visiteur in visiteurs:
        score = score_affinite_1(hote, visiteur)
        row.append(score)
    rows.append(row)

# Écriture des données dans le fichier CSV
with open('scores_affinite.csv', 'w', newline='', encoding='utf-8') as csvfile:
    writer = csv.writer(csvfile, delimiter=';')
    writer.writerow(header)
    writer.writerows(rows)

print("Le fichier 'scores_affinite.csv' a été créé avec succès.")

# Préparation des en-têtes du tableau
header = ['Hôte / Visiteur'] + [visiteur.nom for visiteur in visiteurs]

# Création du tableau
table = PrettyTable()
table.field_names = header

# Ajout des lignes au tableau
for hote in hotes:
    row = [hote.nom]
    for visiteur in visiteurs:
        score = score_affinite_1(hote, visiteur)
        row.append(score)
    table.add_row(row)

# Affichage du tableau dans la console
print(table)
