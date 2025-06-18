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
def score_affinite_3(hote, visiteur):
    score = 10
        # incompatibilité detectee (allergie nourriture || animaux)
    if any(animal in hote.has_animals for animal in visiteur.animal_allergy if animal.lower() != "no"): score += 10
    if any(food in hote.host_food for food in visiteur.food_constraint if food.lower() != "no"): score += 10
    if (hote.history == "other" and visiteur.nom in hote.past_people): score += 10
    if (visiteur.history == "other" and hote.nom in visiteur.past_people): score += 10
        
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

    return round(score, 2)

# Données d'exemple avec contraintes et allergies
hotes = [
    Personne("Alix", "football, trees, cats, tea", "male", "", "19/06/11",
             "cats, rabbits", "peanuts", history="other", past_people="Xavier"),
    
    Personne("Brune", "basket, music, kpop, dogs", "female", "female", "20/09/12",
             "no", "milk", history="", past_people=""),
    
    Personne("Charlie", "running, computer, dogs", "other", "other", "05/12/10",
             "no", "fish", history="", past_people=""),
    
    Personne("Diego", "swimming, running, hiking", "other", "male", "24/04/11",
             "horse, dogs", "watermelon, citrus", history="", past_people="")
]

visiteurs = [
    Personne("William", "football, handball, computer", "male", "male", "26/09/13",
             animal_allergy="no", food_constraint="fish", history="", past_people=""),
    
    Personne("Xavier", "handball, running, cats", "male", "male", "27/09/12",
             animal_allergy="cats, lions", food_constraint="", history="", past_people="Alix"),
    
    Personne("Yvan", "football, trees, dogs, coffee", "other", "other", "12/09/13",
             animal_allergy="no", food_constraint="peanuts, grapefruit", history="other", past_people="Mogadishu"),
    
    Personne("Zorro", "running, hiking, music", "female", "", "10/12/12",
             animal_allergy="cats, dogs", food_constraint="milk", history="", past_people="Carmen")
]


# ajout des autres

# Ajout des nouveaux hôtes
hotes += [
    Personne("Ardente", "cycling, music, dogs, tea", "female", "female", "10/10/11", 
             has_animals="dogs", host_food="kiwi", history="", past_people="Yoann"),
    
    Personne("Bosphore", "reading, chess, cats, piano", "male", "female", "25/03/12", 
             has_animals="cats", host_food="milk", history="", past_people=""),
    
    Personne("Carmen", "swimming, drawing, rabbits, cinema", "other", "other", "18/06/10", 
             has_animals="rabbits", host_food="peanuts", history="other", past_people="Zorro"),
    
    Personne("Douala", "basketball, hiking, reptiles, science", "male", "male", "01/12/11", 
             has_animals="cats, snakes", host_food="fish", history="", past_people="Zorro")
]

# Ajout des nouveaux visiteurs
visiteurs += [
    Personne("Williamelle", "cinema, running, cats, tech", "female", "female", "30/11/13", 
             animal_allergy="dogs", food_constraint="kiwi, watermelon", history="", past_people="Kalgan"),
    
    Personne("Xanthane", "reading, reptiles, football", "male", "female", "09/05/12", 
             animal_allergy="rabbits, horse", food_constraint="peanuts, milk", history="", past_people=""),
    
    Personne("Yaoundé", "swimming, music, birds, trees", "other", "other", "15/07/13", 
             animal_allergy="cats, rabbits", food_constraint="milk, chocolate, kiwi", history="", past_people="Alix"),
    
    Personne("Zoroark", "hiking, science, computers, piano", "male", "female", "02/08/12", 
             animal_allergy="snakes, bears", food_constraint="fish, citrus", history="other", past_people="Alix")
]




# Création du fichier CSV avec les scores
header = [''] + [v.nom for v in visiteurs]
rows = []
for h in hotes:
    row = [h.nom]
    for v in visiteurs:
        score = score_affinite_3(h, v)
        row.append("INF" if score == float('inf') else score)
    rows.append(row)

with open('scores_affinite_ens1ens2.csv', 'w', newline='', encoding='utf-8') as f:
    writer = csv.writer(f, delimiter=';')
    writer.writerow(header)
    writer.writerows(rows)

# Affichage en tableau
table = PrettyTable()
table.field_names = header
for row in rows:
    table.add_row(row)
print(table)
