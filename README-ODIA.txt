SATRUGHAN POS – Android Bluetooth Bridge wrapper

ଏହି ପ୍ରୋଜେକ୍ଟ ଆପଣଙ୍କ ବର୍ତ୍ତମାନର GitHub Pages POS କୁ WebView ଭିତରେ ଖୋଲେ।
ମୂଳ hosted index.html କୁ ଏଠାରେ କପି କିମ୍ବା ବଦଳାଯାଇନାହିଁ।
AndroidPrinter.connect(), AndroidPrinter.print(base64), AndroidPrinter.disconnect()
API ଆପଣଙ୍କ ପୂର୍ବରୁ ଥିବା HTML hooks ସହ ମେଳ ରଖିବାକୁ ତିଆରି।

ସେଟଅପ୍:
1. ଏହି ZIP କୁ extract କରନ୍ତୁ।
2. ଏହାର ସମସ୍ତ files ଆପଣଙ୍କ GitHub `Final` repository ରେ ନୁହେଁ, ନୂଆ Android wrapper repository ରେ upload କରନ୍ତୁ।
3. GitHub → Actions → Build POS APK → Run workflow.
4. Workflow ସଫଳ ହେଲେ Artifacts ରୁ APK download କରନ୍ତୁ।
5. ପ୍ରଥମେ Android Settings → Bluetooth ରେ MT580P କୁ pair କରନ୍ତୁ।
6. APK install କରି Bluetooth permission ଦିଅନ୍ତୁ। App paired MT580P କୁ ଖୋଜି connect କରିବ।

ଗୁରୁତ୍ୱପୂର୍ଣ୍ଣ:
- GitHub Pages ର ପୁରୁଣା PWA ଅପରିବର୍ତ୍ତିତ ରହିବ।
- ଏହି APK ନୂଆ wrapper; Chrome ର Install app ରେ native bridge ଆସିବ ନାହିଁ।
- ଯଦି ଆପଣଙ୍କ MT580P Bluetooth Classic SPP ନୁହେଁ, କିମ୍ବା ଏହାର SPP service ଅଲଗା, ଏହି bridge କାମ କରିବ ନାହିଁ। ଡିଭାଇସ୍ model/serial variant ଯାଞ୍ଚ କରିବା ଦରକାର।
- Android 12+ ରେ BLUETOOTH_CONNECT permission ଆବଶ୍ୟକ।
- ପ୍ରଥମ pairing ଏକଥର Android Bluetooth settings ରେ କରିବା ଦରକାର।
- Release APK signing/Play distribution ପାଇଁ ନିଜ signing key ଦରକାର; workflow ଏଠାରେ debug APK ବନାଏ।
- GitHub ରେ ନିଜେ upload କରିବାକୁ ପଡ଼ିବ; ମୁଁ ଆପଣଙ୍କ account କୁ push କରିନାହିଁ।
