# Jardine-moi 🌿

Bienvenue sur le dépôt du projet **Jardine-moi**, une application de suivi de jardinage développée en Kotlin avec Jetpack Compose.

## 📋 Prérequis

Pour compiler et lancer ce projet, vous devez disposer des éléments suivants :

1.  **Android Studio** (Recommandé) : La version la plus récente (Hedgehog ou ultérieure).
2.  **JDK 17 ou supérieur** : Généralement inclus avec Android Studio.
3.  **Android SDK** : Configuré via Android Studio.
4.  Un **Émulateur Android** (API 30+) ou un **Appareil physique** avec le débogage USB activé.

---

## 🚀 Comment lancer le projet

### Option 1 : Via Android Studio (Recommandé)

C'est la méthode la plus simple et la plus stable :

1.  Ouvrez **Android Studio**.
2.  Sélectionnez **"Open an Existing Project"**.
3.  Choisissez le dossier racine : `C:/Users/gabri/StudioProjects/Jardine-moi/`.
4.  Attendez que la **Synchronisation Gradle** se termine (la barre de progression en bas à droite).
5.  Une fois la synchronisation terminée, appuyez sur le bouton **"Run"** (icône "Play" verte) dans la barre d'outils supérieure.

### Option 2 : Via la ligne de commande (Gradle)

Si vous préférez utiliser un terminal (VS Code, CMD, PowerShell) :

1.  Assurez-vous d'être à la racine du projet dans votre terminal.
2.  **Pour compiler et installer l'application sur un appareil connecté :**
    *   Sur Windows : `.\gradlew installDebug`
    *   Sur Linux/macOS : `./gradlew installDebug`

---

## 🛠️ Dépannage fréquent

*   **Erreur de synchronisation Gradle :** Vérifiez votre connexion internet (le premier lancement télécharge les dépendances).
*   **Version du JDK :** Si Gradle échoue, vérifiez dans vos paramètres (Build, Execution, Deployment > Build Tools > Gradle) que le "Gradle JDK" est bien réglé sur la version 17 ou plus.
*   **Firebase :** Assurez-vous que le fichier `google-services.json` est correctement placé dans le dossier `app/` pour que les fonctionnalités Firebase (Auth, Firestore) fonctionnent.

---
*Développé avec ❤️ pour Jardine-moi.*
