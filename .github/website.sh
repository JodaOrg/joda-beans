
echo "## setup..."
git config --global user.name "Stephen Colebourne (CI)"
git config --global user.email "scolebourne@joda.org"
cd target

echo "## clone..."
git clone https://${GITHUB_TOKEN}@github.com/JodaOrg/jodaorg.github.io.git
cd jodaorg.github.io
git status

echo "## copy..."
rm -rf joda-beans/
cp -R ../site joda-beans/

echo "## update..."
git add -A
git status
git commit --message "Update joda-beans from CI: $GITHUB_ACTION"

echo "## push..."
git push origin main

echo "## tidy..."
cd ..
git clone https://${GITHUB_TOKEN}@github.com/JodaOrg/joda-beans.git
cd joda-beans
git push --delete origin website || true
git push --delete origin website2x || true

echo "## done"
