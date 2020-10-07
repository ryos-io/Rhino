#!/bin/bash 

rm -fR Rhino-Site
rm -fR Rhino.wiki

echo "Checking out the projects"
git clone git@github.com:ryos-io/Rhino-Site.git >/dev/null
git clone git@github.com:ryos-io/Rhino.wiki > /dev/null

echo "Generating Github wiki pages."
ls Rhino-Site/pages/*md | xargs -I {} sed -i '' -e "s|](/|](|g" {}
ls Rhino-Site/pages/*md | xargs -I {} sed -i '' -e 1,8d {}

cp Rhino-Site/pages/*md Rhino.wiki/
cd Rhino.wiki

echo "Pushing the changes to Github"
git commit -am"Auto-updated the documentation from Homepage"
git push 

echo "Update done!"
cd ..

rm -fR Rhino-Site
rm -fR Rhino.wiki
