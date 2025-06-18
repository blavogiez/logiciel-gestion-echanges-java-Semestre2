import csv
from datetime import date, datetime
from dateutil.relativedelta import relativedelta
from prettytable import PrettyTable

# Définition de la classe Personne étendue
class Personne:
    def __init__(self, nom, hobbies, gender, pair_gender, naiss,
                 has_animals=None, host_food=None,
                 animal_allergy=None, food_constraint=None,
                 history=None, past_people=None):
        self.nom = nom
        self.hobbies = [h.strip() for h in hobbies.split(",")]
        self.gender = gender
        self.pair_gender = pair_gender
        self.naiss = datetime.strptime(naiss, "%d/%m/%y").date()

        self.has_animals = [a.strip() for a in has_animals.split(",")] if has_animals else []
        self.host_food = [f.strip() for f in host_food.split(",")] if host_food else []
        self.animal_allergy = [a.strip() for a in animal_allergy.split(",")] if animal_allergy else []
        self.food_constraint = [f.strip() for f in food_constraint.split(",")] if food_constraint else []
        self.history = history.strip() if history else ""
        self.past_people = [p.strip() for p in past_people.split(",")] if past_people else []

# Fonction pour calculer les hobbies en commun
def nombre_hobbies_en_commun(hote, visiteur):
    return len(set(hote.hobbies) & set(visiteur.hobbies))


# Fonction principale de score
def score_affinite_2(hote, visiteur):
    score = 10
    today = date.today()

    # Âge
    hote_age = relativedelta(today, hote.naiss).years
    visiteur_age = relativedelta(today, visiteur.naiss).years
    age_moyen = (hote_age + visiteur_age) / 2
    diff_mois = abs((hote.naiss.year - visiteur.naiss.year) * 12 + (hote.naiss.month - visiteur.naiss.month))

    if diff_mois <= 18:
        score += (diff_mois / 12) * (0.9 ** age_moyen)
        score *= 0.9
    else:
        score += ((diff_mois / 12) * (0.9 ** age_moyen))
        score *= 1.5

    # Genre
    if visiteur.pair_gender and hote.gender != visiteur.pair_gender:
        score *= 1.5
    if hote.pair_gender and visiteur.gender != hote.pair_gender:
        score *= 1.5

    # Hobbies
    N = nombre_hobbies_en_commun(hote, visiteur)
    score *= 0.85 ** N

    # incompatibilité detectee (allergie nourriture || animaux)
    if any(animal in hote.has_animals for animal in visiteur.animal_allergy if animal.lower() != "no") \
            or any(food in hote.host_food for food in visiteur.food_constraint if food.lower() != "no") \
            or (hote.history == "other" and visiteur.nom in hote.past_people) \
            or (visiteur.history == "other" and hote.nom in visiteur.past_people):
        score += 1000

    return round(score, 2)

# Données d'exemple avec contraintes et allergies
hotes = [
    Personne("Alix", "football, trees, cats, tea", "male", "", "19/06/11", "cats, rabbits", "peanuts"),
    Personne("Brune", "basket, music, kpop, dogs", "female", "female", "20/09/12", "no", "milk"),
    Personne("Charlie", "running, computer, dogs", "other", "other", "05/12/10", "no", "fish"),
    Personne("Diego", "swimming, running, hiking", "other", "male", "24/04/11", "horse, dogs", "watermelon")
]

visiteurs = [
    Personne("William", "football, handball, computer", "male", "male", "26/09/13", animal_allergy="no", food_constraint="fish"),
    Personne("Xavier", "handball, running, cats", "male", "male", "27/09/12", animal_allergy="cats, lions", food_constraint=""),
    Personne("Yvan", "football, trees, dogs, coffee", "other", "other", "12/09/13", animal_allergy="no", food_constraint="peanuts, grapefruit"),
    Personne("Zorro", "running, hiking, music", "female", "", "10/12/12", animal_allergy="cats, dogs", food_constraint="milk")
]

# Création du fichier CSV avec les scores
header = [''] + [v.nom for v in visiteurs]
rows = []
for h in hotes:
    row = [h.nom]
    for v in visiteurs:
        score = score_affinite_2(h, v)
        row.append("INF" if score == float('inf') else score)
    rows.append(row)

with open('scores_affinite_ens1.csv', 'w', newline='', encoding='utf-8') as f:
    writer = csv.writer(f, delimiter=';')
    writer.writerow(header)
    writer.writerows(rows)

# Affichage en tableau
table = PrettyTable()
table.field_names = header
for row in rows:
    table.add_row(row)
print(table)
