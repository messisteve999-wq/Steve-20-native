# Steve-20 V1 — Android natif

Projet indépendant (Java + Gradle), cible Android 14 et appareils peu puissants. La compilation est prévue sur GitHub Actions; aucun SDK Android n'est nécessaire dans Termux.

## Fonctions de cette base
- Bulle flottante déplaçable et fenêtre qui s'ouvre/se ferme au toucher.
- Configuration locale de la clé Gemini, des contacts autorisés et du mode automatique (désactivé par défaut).
- Écoute des notifications WhatsApp et WhatsApp Business.
- Mode automatique strictement limité aux titres de notification correspondant exactement aux noms de la liste autorisée.
- Envoi seulement si la notification propose une action de réponse rapide RemoteInput.

## Limites importantes
Cette V1 est une base fonctionnelle de départ, pas une garantie d'envoi universel. WhatsApp doit exposer une action de réponse rapide; sinon aucun envoi n'est fait. Le titre de notification doit correspondre au nom saisi. Les notifications de groupes peuvent être ambiguës : ne mets pas de groupe dans la liste autorisée. Vérifie chaque réponse générée avant d'activer le mode automatique dans les conversations sensibles.

La clé API est stockée localement dans les préférences privées de l'application, mais une clé utilisée dans une application mobile peut être extraite par une personne qui possède l'APK/appareil. Ne publie pas l'APK avec ta clé. Les messages envoyés à Gemini quittent le téléphone pour générer la réponse. L'API Gemini peut avoir des quotas ou des changements de modèle.

## Créer le dépôt
Dans Termux, après avoir extrait le ZIP :
```sh
pkg update -y
pkg install git gh unzip -y
cd ~/Steve-20-native
git init
git branch -M main
git add .
git -c user.name="Steve" -c user.email="messisteve999@gmail.com" commit -m "Create Steve-20 native Android app"
gh repo create messisteve999-wq/Steve-20-native --public --source=. --remote=origin --push
```
Si le dépôt existe déjà, utilise `git remote add origin https://github.com/messisteve999-wq/Steve-20-native.git` puis `git push -u origin main`.

## Récupérer l'APK
GitHub → dépôt Steve-20-native → Actions → “Build Steve-20 Android APK” → dernier workflow vert → Artifacts → `Steve-20-debug-APK.zip`. Extrais ce ZIP pour obtenir `app-debug.apk`.

## Installer et activer
1. Installe `app-debug.apk`.
2. Ouvre Steve-20, saisis ta clé Gemini et les noms autorisés, puis enregistre.
3. Active l'accès aux notifications pour Steve-20 dans les paramètres Android.
4. Autorise l'affichage par-dessus les autres applications.
5. Lance la bulle.
6. Teste d'abord avec le mode automatique désactivé; active-le seulement après vérification.

## Icônes
Les icônes fournies dans cette base sont des placeholders vectoriels génériques. Remplace-les par des images que tu as le droit d'utiliser pour Black Goku et le motif de tatouage si tu souhaites ces visuels exacts.
