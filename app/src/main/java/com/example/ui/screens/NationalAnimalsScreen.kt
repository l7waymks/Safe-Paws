package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ui.theme.PrimaryTeal

data class CountryAnimal(
    val id: String,
    val countryName: String,
    val countryCode: String,
    val flagEmoji: String,
    val animalName: String,
    val animalEmoji: String,
    val continent: String,
    val symbolism: String,
    val facts: List<String>,
    val habitat: String,
    val conservationStatus: String,
    val imageUrl: String,
    val lat: Double,
    val lng: Double
)

val sampleCountryAnimals = listOf(
    CountryAnimal(
        id = "ma",
        countryName = "المغرب",
        countryCode = "MA",
        flagEmoji = "🇲🇦",
        animalName = "أسد الأطلس (أسد بربري)",
        animalEmoji = "🦁",
        continent = "إفريقيا",
        symbolism = "يمثل أسد الأطلس رمز الشجاعة والمهابة والسيادة المغربية التاريخية. عاش في جبال الأطلس وهو أكبر وأضخم سلالات الأسود، ولقب المنتخب الوطني المغربي هو 'أسود الأطلس' تخليداً لهذه القوة الأسطورية.",
        facts = listOf(
            "يمتاز بفرائه الكثيف ولبدته الداكنة الممتدة على بطنه لتحمل برودة جبال الأطلس.",
            "يحتفظ المغرب بسلالات نقية منه في حديقة الحيوانات الوطنية بالرباط ضمن برامج الحماية.",
            "كان رمزاً ملوكياً مغربياً بارزاً عبر العصور."
        ),
        habitat = "سلاسل جبال الأطلس والغابات الجبلية المعتدلة",
        conservationStatus = "منقرض في البرية / محمي في الأسر والمحميات",
        imageUrl = "https://images.unsplash.com/photo-1534188753412-3e26d0d618d6?auto=format&fit=crop&q=80&w=800",
        lat = 31.7917,
        lng = -7.0926
    ),
    CountryAnimal(
        id = "fr",
        countryName = "فرنسا",
        countryCode = "FR",
        flagEmoji = "🇫🇷",
        animalName = "الديك الغالي / الدجاجة (Le Coq Gaulois)",
        animalEmoji = "🐓",
        continent = "أوروبا",
        symbolism = "الديك الغالي هو الرمز الوطني غير الرسمي الأكثر شهرة لفرنسا منذ عهد الرومان وشعب الغال (Gaul). يرمز للشجاعة، اليقظة، الفخر، والإشراق عند بزوغ الفجر، ويزين قمصان المنتخبات الفرنسية والآثار الوطنية.",
        facts = listOf(
            "يعود أصل الرمز للعبة لغوية لاتينية؛ فكلمة 'Gallus' باللاتينية تعني 'بلاد الغال' وتعني أيضاً 'الديك'.",
            "صاح الديك يرمز لليقظة وعدم الاستسلام والفخر القومي الفرنسي.",
            "يعتلي الديك الفرنسي قمة برج إيفل والنصب التذكارية وبوابات قصر الإليزيه."
        ),
        habitat = "المزارع والريف الفرنسي في جميع المقاطعات",
        conservationStatus = "منتشر وآمن",
        imageUrl = "https://images.unsplash.com/photo-1548550023-2bdb3c5beed7?auto=format&fit=crop&q=80&w=800",
        lat = 46.2276,
        lng = 2.2137
    ),
    CountryAnimal(
        id = "es",
        countryName = "إسبانيا",
        countryCode = "ES",
        flagEmoji = "🇪🇸",
        animalName = "الثور الإسباني (El Toro Bravo)",
        animalEmoji = "🐂",
        continent = "أوروبا",
        symbolism = "الثور المقاتل هو الرمز الثقافي الأبرز لإسبانيا، مجسداً القوة، الشجاعة، العنفوان، والكرامة. يشتهر بظله الأسود الأيقوني 'ثور أوزبورن' المنتشر على تلال الطرق السريعة عبر إسبانيا كمعلم سياحي وثقافي معترف به.",
        facts = listOf(
            "يمتلك بنية عضلية قوية وسرعة استجابة مذهلة وطباعاً شجاعة وفريدة.",
            "تنتشر لوحات ومجسمات الثور الضخمة على تلال إسبانيا منذ عام 1956 وأصبحت تراثاً وطنياً.",
            "يرتبط ارتباطاً وثيقاً بالفلكلور الأندلسي ومهرجانات بامبلونا الشهيرة."
        ),
        habitat = "المروج والمراعي الطبيعية المفتوحة (دييسا) في الأندلس وقشتالة",
        conservationStatus = "محمي ومربى بعناية فائقة",
        imageUrl = "https://images.unsplash.com/photo-1545468800-856f6620f77b?auto=format&fit=crop&q=80&w=800",
        lat = 40.4637,
        lng = -3.7492
    ),
    CountryAnimal(
        id = "sa",
        countryName = "المملكة العربية السعودية",
        countryCode = "SA",
        flagEmoji = "🇸🇦",
        animalName = "الصقر الحر والجمل العربي",
        animalEmoji = "🦅",
        continent = "آسيا",
        symbolism = "يمثل الصقر الحر رمز الشموخ، عزة النفس، والدقة في الرؤية، بينما يمثل الجمل 'سفينة الصحراء' رمز الصبر، التحمل، والوفاء في التراث العربي الأصيل للمملكة وتاريخ الجزيرة العربية العريق.",
        facts = listOf(
            "تعتبر الصقارة والصيد بالصقور تراثاً إنسانياً غير مادي مسجلاً لدى اليونسكو.",
            "يستطيع الجمل العربي تحمل العطش لأسابيع والعيش في أقسى الظروف الصحراوية.",
            "يقام في المملكة مهرجان الملك عبدالعزيز للإبل ومزاد الصقور الأكبر عالمياً."
        ),
        habitat = "الصحاري والكثبان الرملية والواحات وجبال السروات",
        conservationStatus = "محمي ومدعوم وطنياً",
        imageUrl = "https://images.unsplash.com/photo-1549608276-5786777e6587?auto=format&fit=crop&q=80&w=800",
        lat = 23.8859,
        lng = 45.0792
    ),
    CountryAnimal(
        id = "dz",
        countryName = "الجزائر",
        countryCode = "DZ",
        flagEmoji = "🇩🇿",
        animalName = "فنك الصحراء (ثعلب الفنك)",
        animalEmoji = "🦊",
        continent = "إفريقيا",
        symbolism = "الفنك هو أصغر الثعالب في العالم والحيوان الوطني للجزائر، ويطلق على المنتخب الجزائري لقب 'الأفناك' أو 'محاربو الصحراء'. يرمز للذكاء، الخفة، والقدرة الخارقة على التكيف مع البيئات الصعبة.",
        facts = listOf(
            "أذناه الكبيرتان تساعدانه على تبديد حرارة الجسم وسماع حركة الفرائس تحت الرمال.",
            "يمتلك فراءً على باطن قدميه لحمايته من حرارة رمال الصحراء الكبرى الملتهبة.",
            "كائن ليلي ذكي ومحبوب جداً بلونه الرملي الجذاب."
        ),
        habitat = "الكثبان الرملية في الصحراء الجزائرية الكبرى",
        conservationStatus = "محمي ضمن المحميات الوطنية",
        imageUrl = "https://images.unsplash.com/photo-1534567153574-2b12153a87f0?auto=format&fit=crop&q=80&w=800",
        lat = 28.0339,
        lng = 1.6596
    ),
    CountryAnimal(
        id = "eg",
        countryName = "مصر",
        countryCode = "EG",
        flagEmoji = "🇪🇬",
        animalName = "عقاب السهوب / صقر صلاح الدين",
        animalEmoji = "🦅",
        continent = "إفريقيا",
        symbolism = "يتوسط عقاب صلاح الدين الذهبي العلم المصري الرسمي ويمثل القوة، العزة، والشموخ التاريخي للحضارة المصرية الممتدة لآلاف السنين من الفراعنة حتى العصر الحديث.",
        facts = listOf(
            "يعتبر العقاب الذهبي رمزاً عسكرياً وتاريخياً للدولة المصرية منذ قرون.",
            "يمتلك نظراً حاداً يفوق نظر الإنسان بثماني مرات وقوة انقضاض مذهلة.",
            "يرتبط أيضاً بقدسية الصقر 'حورس' في النقوش الفرعونية القديمة."
        ),
        habitat = "السهوب والوديان وشبه جزيرة سيناء وضفاف نهر النيل",
        conservationStatus = "محمي بقوانين البيئة",
        imageUrl = "https://images.unsplash.com/photo-1611689342806-0863700ce1e4?auto=format&fit=crop&q=80&w=800",
        lat = 26.8206,
        lng = 30.8025
    ),
    CountryAnimal(
        id = "au",
        countryName = "أستراليا",
        countryCode = "AU",
        flagEmoji = "🇦🇺",
        animalName = "الكنغر الأحمر والكوالا",
        animalEmoji = "🦘",
        continent = "أوقيانوسيا",
        symbolism = "الكنغر هو الرمز الوطني الأول لأستراليا وموجود على شعار النبالة الرسمي مع طائر الإيمو، وسبب اختيارهما أنهما لا يستطيعان المشي للوراء بسهولة، مما يرمز للأمة التي تمضي دائماً إلى الأمام!",
        facts = listOf(
            "الكنغر الأحمر هو أكبر الثدييات الجرابية على وجه الأرض ويمكنه القفز لارتفاع 3 أمتار.",
            "يحمل صغاره في جيب بطني لحمايتهم وتغذيتهم حتى اكتمال نموهم.",
            "يقضي حيوان الكوالا ما يقارب 18 إلى 20 ساعة يومياً في النوم على أشجار الأوكالبتوس."
        ),
        habitat = "المراعي والمناطق شبه الجافة والأحراش الأسترالية",
        conservationStatus = "محمي بموجب القوانين الفيدرالية الأسترالية",
        imageUrl = "https://images.unsplash.com/photo-1589182373726-e4f658ab50f0?auto=format&fit=crop&q=80&w=800",
        lat = -25.2744,
        lng = 133.7751
    ),
    CountryAnimal(
        id = "cn",
        countryName = "الصين",
        countryCode = "CN",
        flagEmoji = "🇨🇳",
        animalName = "الباندا العملاقة (Giant Panda)",
        animalEmoji = "🐼",
        continent = "آسيا",
        symbolism = "الباندا العملاقة هي الكنز القومي للصين وسفيرة السلام والصداقة العالمية فيما يعرف بـ 'دبلوماسية الباندا'. ترمز للسلام والوئام والتعايش السلمي مع الطبيعة.",
        facts = listOf(
            "تقضي الباندا ما يصل إلى 12 ساعة يومياً في تناول نبات الخيزران (البامبو).",
            "حققت الصين نجاحاً مبهراً في حمايتها وإخراجها من قائمة الحيوانات المهددة بالانقراض.",
            "تعتبر جميع حيوانات الباندا في حدائق العالم ملكاً للدولة الصينية بنظام الإعارة."
        ),
        habitat = "غابات الخيزران الجبلية في مقاطعة سيتشوان وجبال قينلينغ",
        conservationStatus = "معرض للخطر الخفيف (في مرحلة التعافي)",
        imageUrl = "https://images.unsplash.com/photo-1564349683136-77e08dba1ef7?auto=format&fit=crop&q=80&w=800",
        lat = 35.8617,
        lng = 104.1954
    ),
    CountryAnimal(
        id = "us",
        countryName = "الولايات المتحدة",
        countryCode = "US",
        flagEmoji = "🇺🇸",
        animalName = "النسر الأصلع والبيسون الأمريكي",
        animalEmoji = "🦅",
        continent = "أمريكا الشمالية",
        symbolism = "النسر الأصلع هو الطائر الوطني الرسمي لأمريكا منذ عام 1782، يرمز للحرية، القوة، والشجاعة المستقلة. كما اعتمد البيسون الأمريكي كثديي وطني كرمز للتاريخ والمروج العظمى.",
        facts = listOf(
            "يمتلك باع جناحين يصل إلى مترين ونصف وقدرة بصرية خارقة لرصد الأسماك من ارتفاعات شاهقة.",
            "يبني أكبر الأعشاش الشجرية بين جميع الطيور بوزن قد يصل لطن كامل.",
            "نجحت برامج الحماية الأمريكية في إنقاذه بعد أن كاد يندثر في القرن العشرين."
        ),
        habitat = "البحيرات الكبرى والغابات الصنوبرية في ألاسكا وأمريكا الشمالية",
        conservationStatus = "آمن ومستقر",
        imageUrl = "https://images.unsplash.com/photo-1579273166152-d725a4e2b755?auto=format&fit=crop&q=80&w=800",
        lat = 37.0902,
        lng = -95.7129
    ),
    CountryAnimal(
        id = "in",
        countryName = "الهند",
        countryCode = "IN",
        flagEmoji = "🇮🇳",
        animalName = "النمر البنغالي الملكي والفيل الآسيوي",
        animalEmoji = "🐅",
        continent = "آسيا",
        symbolism = "النمر البنغالي هو الحيوان الوطني للهند، مجسداً الرشاقة والسرعة والقوة الملكية الجبارة. يحظى بمكانة مقدسة في الأساطير الهندية والتراث الأدبي.",
        facts = listOf(
            "كل نمر يمتلك نمط خطوط فريداً تماماً مثل بصمات أصابع الإنسان.",
            "تضم الهند أكثر من 70% من إجمالي النمور البرية المتبقية في العالم ضمن مشروع نمر الهند.",
            "يعد سباحاً ماهراً جداً ويحب المياه على عكس معظم القطط."
        ),
        habitat = "غابات السندربان المانغروف والمحميات الطبيعية الاستوائية",
        conservationStatus = "مهدد بالانقراض (تحت حماية مشددة)",
        imageUrl = "https://images.unsplash.com/photo-1561731216-c3a4d99437d5?auto=format&fit=crop&q=80&w=800",
        lat = 20.5937,
        lng = 78.9629
    ),
    CountryAnimal(
        id = "ru",
        countryName = "روسيا",
        countryCode = "RU",
        flagEmoji = "🇷🇺",
        animalName = "الدب البني الأوراسي",
        animalEmoji = "🐻",
        continent = "أوروبا",
        symbolism = "الدب الروسي هو الرمز التقليدي الأشهر للأمة الروسية عبر التاريخ، معبراً عن القوة الهائلة، الهيبة، والقدرة على الصمود في أقسى الظروف المناخية وغابات التايغا وسيبيريا.",
        facts = listOf(
            "يقضي فترة الشتاء في بيات شتوي طويل داخل أوكاره تحت الثلوج الكثيفة.",
            "يتمتع بحاسة شم قوية جداً تفوق حاسة الكلاب بأضعاف مضاعفة.",
            "يعد بطلاً رئيسياً في الحكايات الشعبية والفولكلور الروسي القديم."
        ),
        habitat = "غابات التايغا الصنوبرية الشاسعة وجبال الأورال وكامتشاتكا",
        conservationStatus = "مستقر ومحمي",
        imageUrl = "https://images.unsplash.com/photo-1589656966895-2f33e7653819?auto=format&fit=crop&q=80&w=800",
        lat = 61.5240,
        lng = 105.3188
    ),
    CountryAnimal(
        id = "ca",
        countryName = "كندا",
        countryCode = "CA",
        flagEmoji = "🇨🇦",
        animalName = "القندس الكندي (North American Beaver)",
        animalEmoji = "🦫",
        continent = "أمريكا الشمالية",
        symbolism = "القندس هو الحيوان الوطني الرسمي لكندا منذ 1975، يرمز للعمل الدؤوب، الهندسة الطبيعية، والمثابرة. لعب دوراً محورياً في تاريخ استكشاف وبناء كندا عبر تجارة الفراء.",
        facts = listOf(
            "يعد أعظم مهندس معماري في الطبيعة؛ يبني سدوداً مائية تغير مجرى الأنهار لحماية موطنه.",
            "أسنانه الأمامية تنمو باستمرار ويستخدمها لقطع جذوع الأشجار الكبيرة بمهارة.",
            "تزين صورته العملة النقدية الكندية (فئة 5 سنت)."
        ),
        habitat = "الأنهار والبحيرات والغابات الشمالية في عموم كندا",
        conservationStatus = "وافر ومحمي طبيعياً",
        imageUrl = "https://images.unsplash.com/photo-1596797038530-2c107229654b?auto=format&fit=crop&q=80&w=800",
        lat = 56.1304,
        lng = -106.3468
    ),
    CountryAnimal(
        id = "jp",
        countryName = "اليابان",
        countryCode = "JP",
        flagEmoji = "🇯🇵",
        animalName = "طائر الدراج الأخضر وقرد المكاك الياباني",
        animalEmoji = "🦚",
        continent = "آسيا",
        symbolism = "طائر الدراج الأخضر (كيجي) هو الطائر الوطني الرسمي لليابان والمذكور في الأساطير وشعر الواكا القديم. كما تشتهر اليابان بقرود الثلج (المكاك) التي تستمتع بالينابيع الحارة في الشتاء.",
        facts = listOf(
            "يمتلك ريشاً زمردياً براقاً وألواناً زاهية تأسر الأبصار.",
            "يظهر في الحكاية الشعبية اليابانية الشهيرة 'موموتارو' كحليف مخلص للبطل.",
            "تشتهر قرود المكاك بذكائها الاجتماعي واستحمامها في المياه الكبريتية الدافئة."
        ),
        habitat = "الغابات الجبلية ومناطق الينابيع الساخنة في جزر هونشو وكيوشو",
        conservationStatus = "محمي ومقدس تراثياً",
        imageUrl = "https://images.unsplash.com/photo-1503066211613-c17ebc9daef0?auto=format&fit=crop&q=80&w=800",
        lat = 36.2048,
        lng = 138.2529
    ),
    CountryAnimal(
        id = "br",
        countryName = "البرازيل",
        countryCode = "BR",
        flagEmoji = "🇧🇷",
        animalName = "اليغور المرقط (Jaguar) والببغاء القرمزي",
        animalEmoji = "🐆",
        continent = "أمريكا الجنوبية",
        symbolism = "اليغور هو ملك غابات الأمازون المطيرة وأكبر قط في نصف الكرة الغربي. يرمز للقوة الغامضة والجمال البري للتنوع البيولوجي المذهل في البرازيل.",
        facts = listOf(
            "يمتلك أقوى عضة بين جميع القطط الكبيرة مقارنة بحجمه، تمكنه من كسر قواقع السلاحف.",
            "يعد صياداً مائياً استثنائياً ويمضي وقتاً طويلاً في الأنهار.",
            "الببغاء القرمزي يمثل ألوان الطبيعة البرازيلية الفاتنة في حوض الأمازون."
        ),
        habitat = "حوض نهر الأمازون ومستنقعات البانتانال الشاسعة",
        conservationStatus = "مهدد بالقرب من الخطر (برامج حماية نشطة)",
        imageUrl = "https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?auto=format&fit=crop&q=80&w=800",
        lat = -14.2350,
        lng = -51.9253
    ),
    CountryAnimal(
        id = "gb",
        countryName = "بريطانيا (المملكة المتحدة)",
        countryCode = "GB",
        flagEmoji = "🇬🇧",
        animalName = "الأسد البريطاني واليونيكورن",
        animalEmoji = "🦁",
        continent = "أوروبا",
        symbolism = "الأسد البريطاني هو الشعار الوطني للمملكة المتحدة منذ عهد الملك ريتشارد قلب الأسد، يرمز للشجاعة والشرف الملكي وتراه في شعار العرش والمنتخبات الرياضية الإنجليزية.",
        facts = listOf(
            "يحمل الشعار الملكي البريطاني الأسد ممثلاً لإنجلترا وحصان اليونيكورن الأسطوري ممثلاً لاسكتلندا.",
            "تتزين ساحة ترافالغار في لندن بأربعة تماثيل برونزية عملاقة للأسود البريطانية.",
            "يعتبر طائر أبو الحناء الطائر الوطني غير الرسمي الأكثر شعبية في الحدائق البريطانية."
        ),
        habitat = "الحدائق الملكية والمحميات الطبيعية والموروث التاريخي",
        conservationStatus = "رمز وطني تاريخي",
        imageUrl = "https://images.unsplash.com/photo-1546182990-dffeafbe841d?auto=format&fit=crop&q=80&w=800",
        lat = 55.3781,
        lng = -3.4360
    ),
    CountryAnimal(
        id = "de",
        countryName = "ألمانيا",
        countryCode = "DE",
        flagEmoji = "🇩🇪",
        animalName = "النسر الفيدرالي (Bundesadler)",
        animalEmoji = "🦅",
        continent = "أوروبا",
        symbolism = "النسر الفيدرالي هو أقدم شعار للدولة في أوروبا لا يزال مستخدماً حتى اليوم، يرجع إلى عهد شارلمان والإمبراطورية الرومانية المقدسة كرمز للحكم والسيادة والاستقرار.",
        facts = listOf(
            "يعتلي النسر البرلمان الألماني (البوندستاغ) بجدارية ضخمة شهيرة.",
            "يرمز للاعتزاز بالهوية الوطنية والسيادة الدستورية.",
            "يعيش العقاب أبيض الذيل في الغابات والبحيرات المحمية في شمال ألمانيا."
        ),
        habitat = "الغابات السوداء والسهول الشمالية والمحميات الطبيعية",
        conservationStatus = "محمي وصارم الحفظ",
        imageUrl = "https://images.unsplash.com/photo-1598188306155-25e400eb5078?auto=format&fit=crop&q=80&w=800",
        lat = 51.1657,
        lng = 10.4515
    ),
    CountryAnimal(
        id = "it",
        countryName = "إيطاليا",
        countryCode = "IT",
        flagEmoji = "🇮🇹",
        animalName = "الذئب الإيطالي (Lupo Appenninico)",
        animalEmoji = "🐺",
        continent = "أوروبا",
        symbolism = "الذئب الإيطالي هو الرمز الوطني التاريخي غير الرسمي، المرتبط بأسطورة تأسيس مدينة روما الخالدة حيث قامت الذئبة 'لوبا كابيتولينا' بإرضاع الأخوين رومولوس وريموس.",
        facts = listOf(
            "يعيش في جبال الأبينين والألب الوعرة بذكاء وتكافل اجتماعي مذهل.",
            "نجحت إيطاليا في زيادة أعداده بعد حمايته الصارمة في السبعينات.",
            "يعد رمزاً للشجاعة والولاء وحماية الأسرة والقطيع."
        ),
        habitat = "سلسلة جبال الأبينين والحدائق الوطنية الإيطالية",
        conservationStatus = "محمي بموجب القوانين الأوروبية",
        imageUrl = "https://images.unsplash.com/photo-1564865878688-9a244444042a?auto=format&fit=crop&q=80&w=800",
        lat = 41.8719,
        lng = 12.5674
    ),
    CountryAnimal(
        id = "ae",
        countryName = "الإمارات العربية المتحدة",
        countryCode = "AE",
        flagEmoji = "🇦🇪",
        animalName = "المها العربي والصقر الحر",
        animalEmoji = "🦌",
        continent = "آسيا",
        symbolism = "المها العربي بقرنيه الطويلين وجسده الأبيض الناصع هو رمز الجمال والأصالة والصمود في الصحراء، بينما يزين الصقر شعار دولة الإمارات رمزاً للعزة والقوة والرؤية المستقبلية الثاقبة.",
        facts = listOf(
            "قادت الإمارات جهوداً عالمية رائدة لإعادة توطين المها العربي بعد أن شارف على الاندثار.",
            "يعتبر الصقر جزءاً لا يتجزأ من التراث الإماراتي الأصيل وتوضع صورته على العملات الرسمية.",
            "المها يستطيع الشعور بهطول الأمطار على بعد عشرات الكيلومترات والتحرك نحوها."
        ),
        habitat = "محميات دبي وأبوظبي الصحراوية وصحراء الربع الخالي",
        conservationStatus = "محمي ومزدهر في المحميات الملكية",
        imageUrl = "https://images.unsplash.com/photo-1535083783855-76ae62b2914e?auto=format&fit=crop&q=80&w=800",
        lat = 23.4241,
        lng = 53.8478
    ),
    CountryAnimal(
        id = "nz",
        countryName = "نيوزيلندا",
        countryCode = "NZ",
        flagEmoji = "🇳🇿",
        animalName = "طائر الكيوي (Kiwi Bird)",
        animalEmoji = "🥝",
        continent = "أوقيانوسيا",
        symbolism = "طائر الكيوي الفريد هو الرمز الأكثر ارتباطاً بنيوزيلندا لدرجة أن النيوزيلنديين يطلقون على أنفسهم شعبياً اسم 'كيويز'. طائر ليلي وديع لا يطير، ويعبر عن الطبيعة الفريدة والمعزولة للجزر النيوزيلندية.",
        facts = listOf(
            "طائر غير قادر على الطيران وريشه يشبه الفراء وشعيرات الثدييات.",
            "يضع بيضة ضخمة جداً تصل إلى 20% من حجم جسم الأنثى (أكبر نسبة في عالم الطيور).",
            "يمتلك فتحتي أنف في نهاية منقاره الطويل لاستشعار الحشرات في التربة."
        ),
        habitat = "الغابات المطيرة المعتدلة والمحميات الخالية من المفترسات",
        conservationStatus = "معرض للخطر وتحت حماية مشددة جداً",
        imageUrl = "https://images.unsplash.com/photo-1516467508483-a7212febe31a?auto=format&fit=crop&q=80&w=800",
        lat = -40.9006,
        lng = 174.8860
    ),
    CountryAnimal(
        id = "za",
        countryName = "جنوب إفريقيا",
        countryCode = "ZA",
        flagEmoji = "🇿🇦",
        animalName = "غزال السبرينغبوك (القفاز)",
        animalEmoji = "🦌",
        continent = "إفريقيا",
        symbolism = "السبرينغبوك هو الحيوان الوطني لجنوب إفريقيا ورمز منتخب الرغبي الوطني الفائز بكأس العالم 'Springboks'. يشتهر بقدرته على القفز العمودي المتكرر في الهواء بسرعة وخفة مذهلة.",
        facts = listOf(
            "يستطيع القفز لارتفاع يصل إلى 3 أمتار والركض بسرعة 88 كم/ساعة لمراوغة الفهود.",
            "يقوم بحركة استعراضية شهيرة تدعى 'pronking' لإبهار الأعداء بلياقته.",
            "يعيش في قطعان متماسكة في سهول الكالاهاري والسافانا."
        ),
        habitat = "محمية كروجر الوطنية وسهول الكاروو والسافانا الجافة",
        conservationStatus = "وافر ومحمي في المحميات الإفريقية",
        imageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&q=80&w=800",
        lat = -30.5595,
        lng = 22.9375
    )
)

data class QuizQuestion(
    val country: CountryAnimal,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NationalAnimalsScreen(
    onBack: () -> Unit,
    onNavigateToMap: ((Double, Double, String) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedContinent by remember { mutableStateOf("الكل") }
    var selectedAnimalDetail by remember { mutableStateOf<CountryAnimal?>(null) }
    var activeTab by remember { mutableStateOf("encyclopedia") } // "encyclopedia", "map", or "quiz"
    var mapSelectedCountryId by remember { mutableStateOf<String?>(null) }

    val continents = listOf("الكل", "إفريقيا", "أوروبا", "آسيا", "أمريكا الشمالية", "أمريكا الجنوبية", "أوقيانوسيا")

    val filteredList = remember(searchQuery, selectedContinent) {
        sampleCountryAnimals.filter { item ->
            val matchContinent = selectedContinent == "الكل" || item.continent == selectedContinent
            val matchSearch = searchQuery.isBlank() ||
                    item.countryName.contains(searchQuery, ignoreCase = true) ||
                    item.animalName.contains(searchQuery, ignoreCase = true) ||
                    item.symbolism.contains(searchQuery, ignoreCase = true)
            matchContinent && matchSearch
        }
    }

    if (activeTab == "map") {
        NationalAnimalsMapScreen(
            initialCountryId = mapSelectedCountryId,
            onBack = { activeTab = "encyclopedia" }
        )
        return
    }

    if (selectedAnimalDetail != null) {
        CountryAnimalDetailDialog(
            animal = selectedAnimalDetail!!,
            onDismiss = { selectedAnimalDetail = null },
            onLocateOnMap = {
                val a = selectedAnimalDetail!!
                selectedAnimalDetail = null
                mapSelectedCountryId = a.id
                activeTab = "map"
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🌍 موسوعة الحيوانات الوطنية للدول",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "عودة", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryTeal,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Mode Switcher: Encyclopedia vs Interactive Map vs Quiz
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "encyclopedia" to "الموسوعة 📖",
                    "map" to "خريطة الحدود والحيوانات 🗺️",
                    "quiz" to "التحدي 🎯"
                ).forEach { (tabKey, label) ->
                    val isSelected = activeTab == tabKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { activeTab = tabKey }
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (activeTab == "encyclopedia") {
                // Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("search_country_animals"),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "بحث",
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "ابحث بالدولة أو الحيوان (فرنسا، إسبانيا، المغرب...)",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            fontSize = 13.sp
                                        )
                                    }
                                    innerTextField()
                                }
                                if (searchQuery.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "مسح",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { searchQuery = "" }
                                    )
                                }
                            }
                        }
                    )
                }

                // Continent Filter Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(continents) { cont ->
                        val isSelected = selectedContinent == cont
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedContinent = cont },
                            label = { Text(cont, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryTeal,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Quick Summary Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryTeal.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "💡", fontSize = 22.sp)
                        Text(
                            text = "لكل دولة حيوان يميز تاريخها وثقافتها: فرنسا بالديك 🐓، إسبانيا بالثور 🐂، المغرب بأسد الأطلس 🦁 والسعودية بالصقر والجمل 🦅!",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Grid of Countries & National Animals
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        CountryAnimalCard(
                            item = item,
                            onClick = { selectedAnimalDetail = item }
                        )
                    }
                }
            } else {
                // Interactive Quiz Game View
                NationalAnimalsQuizView(onNavigateToDetail = { selectedAnimalDetail = it })
            }
        }
    }
}

@Composable
fun CountryAnimalCard(
    item: CountryAnimal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.animalName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Country Flag Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = item.flagEmoji, fontSize = 14.sp)
                        Text(
                            text = item.countryName,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Animal Emoji Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    shape = CircleShape,
                    color = PrimaryTeal
                ) {
                    Text(
                        text = item.animalEmoji,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.animalName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = item.symbolism,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = item.continent,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "تفاصيل 👈",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal
                    )
                }
            }
        }
    }
}

@Composable
fun CountryAnimalDetailDialog(
    animal: CountryAnimal,
    onDismiss: () -> Unit,
    onLocateOnMap: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = animal.imageUrl,
                        contentDescription = animal.animalName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                    }

                    // Bottom Gradient & Country Name
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = animal.flagEmoji, fontSize = 26.sp)
                            Column {
                                Text(
                                    text = "${animal.countryName} - ${animal.animalName}",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "القارة: ${animal.continent}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Symbolism & Cultural Story
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryTeal.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "لماذا اشتهرت الدولة بهذا الحيوان؟",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryTeal
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = animal.symbolism,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Key Facts
                    Text(
                        text = "📌 حقائق ومعلومات مميزة:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    animal.facts.forEach { fact ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "✨", fontSize = 14.sp)
                            Text(
                                text = fact,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Habitat & Conservation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "🌲 البيئة والموطن", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = animal.habitat, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "🛡️ حالة الحفظ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = animal.conservationStatus, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Map action button
                    Button(
                        onClick = onLocateOnMap,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("عرض موقع ${animal.countryName} على الخريطة 🗺️", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun NationalAnimalsQuizView(
    onNavigateToDetail: (CountryAnimal) -> Unit
) {
    val allQuestions = remember {
        sampleCountryAnimals.map { country ->
            val otherAnimals = sampleCountryAnimals.filter { it.id != country.id }.shuffled().take(3)
            val options = (listOf(country) + otherAnimals).shuffled()
            val correctIdx = options.indexOfFirst { it.id == country.id }
            QuizQuestion(
                country = country,
                questionText = "ما هو الحيوان الوطني والرمز الشهير لدولة ${country.flagEmoji} ${country.countryName}؟",
                options = options.map { "${it.animalEmoji} ${it.animalName}" },
                correctIndex = correctIdx,
                explanation = country.symbolism
            )
        }.shuffled()
    }

    var currentIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var showExplanation by remember { mutableStateOf(false) }

    val currentQ = allQuestions.getOrNull(currentIndex % allQuestions.size) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Score Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryTeal)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "🎯 نقاط التحدي", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Text(text = "$score نقطة", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "🔥 سلسلة الإجابات", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Text(text = "$streak متتالية", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Question Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "سؤال ${(currentIndex % allQuestions.size) + 1} من ${allQuestions.size}",
                    fontSize = 12.sp,
                    color = PrimaryTeal,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = currentQ.questionText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Options
                currentQ.options.forEachIndexed { index, option ->
                    val isSelected = selectedOptionIndex == index
                    val isCorrect = index == currentQ.correctIndex

                    val (containerColor, textColor, borderColor) = when {
                        showExplanation && isCorrect -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Color(0xFF4CAF50))
                        showExplanation && isSelected && !isCorrect -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), Color(0xFFEF5350))
                        isSelected -> Triple(PrimaryTeal.copy(alpha = 0.15f), PrimaryTeal, PrimaryTeal)
                        else -> Triple(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), MaterialTheme.colorScheme.onSurface, Color.Transparent)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(containerColor)
                            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable(enabled = !showExplanation) {
                                selectedOptionIndex = index
                                showExplanation = true
                                if (index == currentQ.correctIndex) {
                                    score += 10
                                    streak += 1
                                } else {
                                    streak = 0
                                }
                            }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected || (showExplanation && isCorrect)) FontWeight.Bold else FontWeight.Medium,
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )
                            if (showExplanation) {
                                if (isCorrect) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "صحيح", tint = Color(0xFF2E7D32))
                                } else if (isSelected) {
                                    Icon(Icons.Default.Cancel, contentDescription = "خطأ", tint = Color(0xFFC62828))
                                }
                            }
                        }
                    }
                }

                // Explanation & Next Button
                if (showExplanation) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryTeal.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "💡 معلومة سريعة:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryTeal)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = currentQ.explanation, fontSize = 12.sp, lineHeight = 17.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToDetail(currentQ.country) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("بطاقة الدولة 📖", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                selectedOptionIndex = null
                                showExplanation = false
                                currentIndex++
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text("السؤال التالي ➡️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
