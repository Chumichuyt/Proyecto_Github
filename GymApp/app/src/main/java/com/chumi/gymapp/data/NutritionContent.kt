package com.chumi.gymapp.data

data class Tip(val category: String, val emoji: String, val title: String, val text: String)

data class MealIdea(
    val id: String,
    val name: String,
    val meal: Meal,
    val goals: Set<Goal>,
    val minutes: Int,
    val ingredients: List<Pair<String, Double>>, // id de alimento + gramos
    val steps: String,
    val why: String,
)

private val ALL = Goal.entries.toSet()
private val CUT = setOf(Goal.LOSE, Goal.RECOMP, Goal.MAINTAIN)
private val BULK = setOf(Goal.GAIN, Goal.MAINTAIN)

/** Consejos e ideas de un nutricionista deportivo con muchos años de gimnasio. */
object NutritionContent {

    val tips: List<Tip> = listOf(
        // Fundamentos
        Tip("Fundamentos", "⚖️", "Las calorías mandan",
            "Si quieres perder grasa necesitas comer algo menos de lo que gastas; si quieres ganar músculo, algo más. Ningún alimento \"engorda\" o \"adelgaza\" por sí solo: es el total de la semana lo que cuenta. Los macros deciden si lo que ganas o pierdes es músculo o grasa."),
        Tip("Fundamentos", "🎯", "La regla del 80/20",
            "El 80 % de tu dieta: comida real (carne, pescado, huevos, lácteos, legumbres, arroz, patata, fruta, verdura, aceite de oliva). El 20 % restante, lo que te apetezca. Una dieta que puedes mantener años gana siempre a una perfecta que dejas en 3 semanas."),
        Tip("Fundamentos", "📏", "Pesa la comida unas semanas",
            "Pesar los alimentos crudos durante 2-4 semanas te enseña a calcular raciones \"a ojo\" para siempre. El error más común: el aceite. Un chorrito \"pequeño\" son fácilmente 20-30 g, es decir, 180-270 kcal."),
        Tip("Fundamentos", "📊", "Cómo pesarte bien",
            "Pésate por la mañana, después de ir al baño y antes de comer o beber. El peso diario baila 1-2 kg por agua, sal, glucógeno y digestión. Mira la media semanal: es lo que usa el coach de la app para ajustar tus calorías."),
        // Proteína
        Tip("Proteína", "🥩", "Cuánta proteína necesitas",
            "Para ganar músculo, entre 1,6 y 2,2 g por kg de peso al día. En definición conviene ir a la parte alta (2,0-2,4 g/kg) para proteger el músculo. Más de eso no aporta beneficio extra, solo sacia más."),
        Tip("Proteína", "🍽️", "Repártela en 3-5 tomas",
            "Mejor 4 tomas de 30-40 g que una cena con 120 g. Cada toma con unos 0,4 g/kg estimula al máximo la síntesis de músculo. Fuentes top: pollo, pavo, pescado, huevos, claras, skyr, queso fresco batido, legumbres, tofu y whey."),
        Tip("Proteína", "🌙", "Proteína antes de dormir",
            "30-40 g de proteína de digestión lenta antes de dormir (queso fresco batido, skyr, caseína) ayuda a la recuperación nocturna. Un clásico del culturismo con respaldo científico."),
        Tip("Proteína", "🌱", "Si eres vegetariano o vegano",
            "Combina legumbres + cereales a lo largo del día, usa tofu, tempeh, seitán, soja texturizada y proteína vegetal en polvo. Sube un poco la cantidad total (≈ +10 %) porque la proteína vegetal es algo menos rica en leucina."),
        // Carbohidratos
        Tip("Carbohidratos", "🍚", "Los carbohidratos son tu gasolina",
            "Si entrenas fuerza, entre 3 y 6 g por kg al día te ayudan a rendir y recuperar. No engordan por sí mismos: engorda el exceso de calorías. Prioriza arroz, patata, avena, pan integral, legumbres y fruta."),
        Tip("Carbohidratos", "⏰", "Colócalos alrededor del entreno",
            "Pon buena parte de los carbohidratos del día en la comida de antes y de después de entrenar. Rendirás más y repondrás glucógeno. Por la noche puedes comer carbohidratos sin problema: dormirás incluso mejor."),
        Tip("Carbohidratos", "🥦", "Fibra: 25-35 g al día",
            "La fibra sacia, cuida tu intestino y mejora tu salud cardiovascular. Una buena regla: 14 g por cada 1.000 kcal. Fruta entera (no zumo), verdura, legumbres, avena e integrales."),
        // Grasas
        Tip("Grasas", "🫒", "No elimines la grasa",
            "Las grasas son clave para tus hormonas (incluida la testosterona). No bajes del 20 % de las calorías ni de 0,6 g/kg. Aceite de oliva virgen extra, frutos secos, aguacate, huevos y pescado azul."),
        Tip("Grasas", "🐟", "Pescado azul 2 veces por semana",
            "Salmón, sardinas, caballa o atún aportan omega-3, bueno para el corazón y la inflamación. Si no comes pescado, valora un suplemento de omega-3 consultándolo con tu médico."),
        // Entreno
        Tip("Entreno", "⚡", "Qué comer antes de entrenar",
            "1-3 horas antes: carbohidratos + algo de proteína y poca grasa y fibra (digestión rápida). Ejemplo: tostadas con plátano y miel + yogur, o arroz con pollo. Si entrenas nada más levantarte, un plátano o un café con leche bastan."),
        Tip("Entreno", "💪", "Después de entrenar",
            "La famosa \"ventana anabólica\" de 30 minutos es más amplia de lo que se creía: tienes varias horas. Haz una comida con 30-40 g de proteína y carbohidratos dentro de las 2 horas siguientes y listo."),
        Tip("Entreno", "☕", "La cafeína funciona",
            "3-6 mg por kg de peso, 30-60 minutos antes de entrenar, mejora la fuerza y la resistencia. Con un café o dos sueles tener bastante. Evítala 6-8 horas antes de dormir: el sueño también es entreno."),
        // Objetivos
        Tip("Definición", "🔥", "Cómo hacer una definición que funcione",
            "Déficit moderado: perder entre el 0,5 y el 1 % de tu peso a la semana. Proteína alta, sigue entrenando pesado (es la señal para mantener el músculo) y añade pasos diarios (8-12k). Si tu fuerza cae mucho, el déficit es demasiado grande."),
        Tip("Definición", "🥗", "Trucos para pasar menos hambre",
            "Llena medio plato de verdura, prioriza proteína en cada comida, bebe agua, usa alimentos de mucho volumen y pocas calorías (fresas, sandía, patata cocida, claras, skyr) y deja los refrescos normales por los zero."),
        Tip("Volumen", "📈", "Volumen limpio, no \"comer de todo\"",
            "Superávit pequeño: 200-300 kcal sobre tu mantenimiento. Gana un 0,25-0,5 % de tu peso al mes si ya tienes experiencia (algo más si eres principiante). Ganar peso muy rápido es ganar sobre todo grasa que luego tendrás que perder."),
        Tip("Volumen", "🥤", "Si te cuesta comer",
            "Usa calorías líquidas y densas: batidos con leche entera, avena, plátano, crema de cacahuete y whey (más de 900 kcal en un vaso). Añade aceite de oliva a las comidas y frutos secos como snack."),
        Tip("Recomposición", "🔄", "¿Ganar músculo y perder grasa a la vez?",
            "Sí, si eres principiante, vuelves tras un parón o tienes un % de grasa alto. Come cerca de tu mantenimiento (ligero déficit), proteína alta (2,2 g/kg) y entrena con progresión. La báscula se moverá poco: fíjate en la cintura, las fotos y tu fuerza."),
        // Hidratación y descanso
        Tip("Hábitos", "💧", "Hidratación",
            "Unos 35 ml por kg de peso al día, más 500-750 ml por cada hora de entreno. Si tu orina es de color amarillo pálido, vas bien. Con mucho calor o sudor, añade sal o bebida isotónica."),
        Tip("Hábitos", "😴", "Duerme 7-9 horas",
            "Dormir poco en una dieta hace que pierdas más músculo y menos grasa, y dispara el hambre. El sueño es el suplemento más barato y potente que existe."),
        Tip("Hábitos", "🍺", "El alcohol",
            "Aporta 7 kcal por gramo sin nutrientes y reduce la recuperación muscular. No hace falta prohibirlo, pero si bebes, que sea poco, y elige cerveza o vino antes que cubatas con refresco."),
        // Suplementos
        Tip("Suplementos", "🧂", "Creatina: el suplemento rey",
            "Creatina monohidrato, 3-5 g al día, todos los días, a la hora que quieras. No hace falta fase de carga. Mejora fuerza y masa muscular y es el suplemento más estudiado. Puede subirte 1-2 kg de peso por agua dentro del músculo (es buena señal)."),
        Tip("Suplementos", "🥛", "Proteína en polvo",
            "No es mágica: es comida (leche) en formato cómodo. Úsala si no llegas a tu proteína con comida normal. Whey o isolate tras entrenar o en el desayuno; caseína o queso batido por la noche."),
        Tip("Suplementos", "🚫", "En lo que no gastaría dinero",
            "BCAA (si ya tomas suficiente proteína no aportan nada), quemagrasas, \"detox\" y la mayoría de pre-entrenos caros (lo que funciona en ellos suele ser la cafeína). Vitamina D u omega-3 solo si tu médico ve que te faltan."),
        // Mitos
        Tip("Mitos", "🕗", "\"Cenar carbohidratos engorda\"",
            "Falso. Lo que importa es el total del día. Muchos culturistas de élite cenan arroz o patata y además duermen mejor y entrenan con más energía a la mañana siguiente."),
        Tip("Mitos", "🕒", "\"Hay que comer cada 3 horas\"",
            "Falso. La frecuencia de comidas no acelera el metabolismo. Haz las comidas que te vayan bien (3, 4 o 5) y que cada una tenga buena proteína."),
        Tip("Mitos", "🥚", "\"Los huevos suben el colesterol\"",
            "En la mayoría de personas sanas, comer 1-3 huevos al día no empeora el colesterol en sangre. Son una de las mejores proteínas que existen y además son baratos."),
        Tip("Mitos", "🍌", "\"La fruta engorda por el azúcar\"",
            "La fruta entera tiene fibra, agua, vitaminas y sacia mucho. Es imposible engordar por comer fruta si tu balance de calorías está bien. Otra cosa son los zumos: mejor fruta entera."),
        // Salud
        Tip("Salud", "⚠️", "Cuándo acudir a un profesional",
            "Si tienes alguna enfermedad (diabetes, problemas de riñón o tiroides…), estás embarazada, has tenido problemas con la comida o tomas medicación, consulta con un dietista-nutricionista colegiado y tu médico antes de cambiar tu dieta."),
    )

    fun tipOfTheDay(day: Long): Tip = tips[(day % tips.size).toInt().let { if (it < 0) it + tips.size else it }]

    val ideas: List<MealIdea> = listOf(
        // ---- Desayunos ----
        MealIdea("porridge", "Porridge proteico de avena y plátano", Meal.BREAKFAST, ALL, 8,
            listOf("food_oats" to 60.0, "food_milk_semi" to 250.0, "food_whey" to 30.0, "food_banana" to 120.0, "food_peanut_butter" to 10.0),
            "Cuece la avena con la leche 3-4 minutos removiendo. Fuera del fuego añade la proteína y mezcla bien. Corona con el plátano en rodajas y la crema de cacahuete.",
            "Carbohidratos de absorción lenta + 40 g de proteína: energía estable toda la mañana."),
        MealIdea("eggs_toast", "Tostadas integrales con huevos revueltos y aguacate", Meal.BREAKFAST, ALL, 10,
            listOf("food_bread_whole" to 60.0, "food_egg" to 110.0, "food_egg_white" to 66.0, "food_avocado" to 40.0, "food_tomato" to 100.0),
            "Bate 2 huevos y 2 claras con sal y pimienta y cuájalos a fuego bajo. Tuesta el pan, unta el aguacate, añade tomate en rodajas y los huevos encima.",
            "Grasas buenas, proteína de máxima calidad y fibra. Sacia muchísimo."),
        MealIdea("skyr_bowl", "Bol de skyr con frutos rojos y nueces", Meal.BREAKFAST, CUT, 3,
            listOf("food_skyr" to 250.0, "food_blueberries" to 80.0, "food_strawberries" to 100.0, "food_walnuts" to 15.0, "food_honey" to 10.0),
            "Pon el skyr en un bol, añade la fruta troceada, las nueces picadas y un hilo de miel.",
            "Mucha proteína con pocas calorías y mucho volumen: ideal en definición."),
        MealIdea("pancakes", "Tortitas de avena y claras", Meal.BREAKFAST, ALL, 12,
            listOf("food_oats" to 50.0, "food_egg_white" to 165.0, "food_banana" to 60.0, "food_quark" to 125.0, "food_strawberries" to 100.0),
            "Tritura avena, claras y medio plátano hasta tener una masa. Haz las tortitas en una sartén antiadherente a fuego medio (1-2 min por lado). Sírvelas con queso batido y fresas.",
            "Un desayuno de \"capricho\" con más de 45 g de proteína y muy poca grasa."),
        MealIdea("gainer_shake", "Batido de volumen casero (900+ kcal)", Meal.BREAKFAST, setOf(Goal.GAIN), 3,
            listOf("food_milk_whole" to 400.0, "food_oats" to 80.0, "food_whey" to 30.0, "food_banana" to 120.0, "food_peanut_butter" to 32.0),
            "Bate todo en la batidora 30-40 segundos. Si te sienta pesado, tómalo en dos veces.",
            "Para quien \"no puede comer más\": muchas calorías de calidad en un vaso."),
        // ---- Comidas ----
        MealIdea("rice_chicken", "Arroz con pollo y verduras salteadas", Meal.LUNCH, ALL, 25,
            listOf("food_rice_raw" to 80.0, "food_chicken_raw" to 180.0, "food_pepper" to 100.0, "food_zucchini" to 150.0, "food_olive_oil" to 10.0),
            "Cuece el arroz. Mientras, saltea el pollo en dados con el aceite, añade pimiento y calabacín y cocina 5-6 minutos. Mezcla con el arroz, sal, pimienta y un toque de soja o pimentón.",
            "El clásico del gym por algo: proteína magra, carbohidrato fácil de digerir y verdura."),
        MealIdea("bolognese", "Pasta boloñesa fitness", Meal.LUNCH, ALL, 25,
            listOf("food_pasta_raw" to 90.0, "food_beef_mince5" to 150.0, "food_tomato_sauce" to 100.0, "food_onion" to 50.0, "food_olive_oil" to 5.0, "food_mozzarella" to 20.0),
            "Sofríe la cebolla en el aceite, añade la carne y dórala. Incorpora el tomate y deja 10 minutos. Mezcla con la pasta cocida y un poco de mozzarella rallada.",
            "Sabe a comida trampa pero tiene más de 50 g de proteína y grasa controlada."),
        MealIdea("lentils", "Lentejas con verduras y huevo duro", Meal.LUNCH, ALL, 20,
            listOf("food_lentils" to 300.0, "food_carrot" to 80.0, "food_onion" to 50.0, "food_egg" to 55.0, "food_olive_oil" to 10.0),
            "Con lentejas de bote: sofríe cebolla y zanahoria, añade las lentejas lavadas, un poco de agua, pimentón y comino. 10 minutos y sirve con huevo duro picado.",
            "Barata, con muchísima fibra, hierro y proteína. Añade un cítrico de postre para absorber mejor el hierro."),
        MealIdea("salmon_potato", "Salmón con patata asada y brócoli", Meal.LUNCH, ALL, 30,
            listOf("food_salmon" to 150.0, "food_potato" to 250.0, "food_broccoli" to 200.0, "food_olive_oil" to 5.0),
            "Asa la patata en gajos con el aceite a 200 °C 25 minutos. Los últimos 12 minutos añade el salmón y el brócoli a la bandeja. Sal, limón y eneldo.",
            "Omega-3 del salmón, potasio de la patata y vitaminas del brócoli. Todo en una bandeja."),
        MealIdea("quinoa_bowl", "Bowl de quinoa, garbanzos y atún", Meal.LUNCH, CUT, 15,
            listOf("food_quinoa" to 60.0, "food_chickpeas" to 120.0, "food_tuna_water" to 112.0, "food_tomato" to 150.0, "food_cucumber" to 100.0, "food_olive_oil" to 10.0),
            "Cuece la quinoa 12 minutos y enfríala. Mezcla con garbanzos, 2 latas de atún, tomate y pepino. Aliña con aceite, limón y sal.",
            "Plato frío perfecto para llevar en táper al trabajo."),
        MealIdea("caesar", "Ensalada César fit", Meal.LUNCH, CUT, 15,
            listOf("food_chicken_grill" to 150.0, "food_lettuce" to 150.0, "food_bread_whole" to 30.0, "food_cheese_cured" to 15.0, "food_yogurt" to 60.0, "food_olive_oil" to 5.0),
            "Pollo a la plancha en tiras sobre la lechuga, picatostes de pan integral tostado y parmesano. Salsa: yogur + aceite + limón + ajo en polvo + mostaza.",
            "Todo el sabor de la César con la mitad de calorías gracias a la salsa de yogur."),
        // ---- Cenas ----
        MealIdea("hake_oven", "Merluza al horno con verduras y patata", Meal.DINNER, ALL, 30,
            listOf("food_hake" to 200.0, "food_potato" to 150.0, "food_zucchini" to 150.0, "food_eggplant" to 100.0, "food_onion" to 50.0, "food_olive_oil" to 10.0),
            "Hornea la patata en láminas finas con cebolla y el aceite 15 minutos a 200 °C. Añade las verduras y la merluza encima y 12 minutos más.",
            "Cena ligera, muy saciante y con proteína de digestión fácil."),
        MealIdea("omelette", "Tortilla francesa de 3 huevos con ensalada", Meal.DINNER, ALL, 10,
            listOf("food_egg" to 165.0, "food_lettuce" to 100.0, "food_tomato" to 150.0, "food_bread_whole" to 30.0, "food_olive_oil" to 5.0),
            "Bate los huevos, cuaja la tortilla con unas gotas de aceite y acompaña con ensalada aliñada y una rebanada de pan integral.",
            "La cena rápida por excelencia: lista en 10 minutos."),
        MealIdea("wrap", "Wrap de pollo, hummus y verduras", Meal.DINNER, ALL, 10,
            listOf("food_wrap" to 60.0, "food_chicken_grill" to 120.0, "food_hummus" to 30.0, "food_lettuce" to 50.0, "food_tomato" to 80.0),
            "Calienta la tortilla 20 segundos, unta el hummus y rellena con pollo, lechuga y tomate. Enrolla y a la sartén 1 minuto por lado.",
            "Comida \"de bar\" hecha en casa con macros de campeón."),
        MealIdea("tofu_scramble", "Revuelto de tofu con espinacas (vegano)", Meal.DINNER, ALL, 12,
            listOf("food_tofu" to 200.0, "food_spinach" to 100.0, "food_mushrooms" to 100.0, "food_bread_whole" to 60.0, "food_olive_oil" to 10.0),
            "Desmenuza el tofu, saltéalo con los champiñones, cúrcuma, pimienta y sal. Añade las espinacas al final. Sirve con pan integral tostado.",
            "Alternativa vegetal con proteína completa de la soja."),
        // ---- Pre-entreno ----
        MealIdea("pre_toast", "Tostadas con plátano y miel", Meal.PRE_WORKOUT, ALL, 5,
            listOf("food_bread_white" to 80.0, "food_banana" to 120.0, "food_honey" to 15.0, "food_turkey_cold" to 45.0),
            "Tuesta el pan, pon el pavo, el plátano en rodajas y un hilo de miel. Tómalo 60-90 minutos antes de entrenar.",
            "Carbohidratos rápidos y fáciles de digerir: energía sin pesadez."),
        MealIdea("pre_ricecakes", "Tortitas de arroz con pavo y mermelada", Meal.PRE_WORKOUT, CUT, 3,
            listOf("food_rice_cakes" to 32.0, "food_turkey_cold" to 60.0, "food_jam" to 20.0, "food_coffee" to 60.0),
            "4 tortitas: dos con pavo y dos con mermelada. Con un café solo 30-45 minutos antes.",
            "Ligero, rápido y con cafeína: perfecto si entrenas a primera hora."),
        // ---- Post-entreno ----
        MealIdea("recovery_shake", "Batido de recuperación", Meal.POST_WORKOUT, ALL, 3,
            listOf("food_milk_skim" to 300.0, "food_whey" to 30.0, "food_banana" to 120.0, "food_oats" to 30.0),
            "Bate todo. Tómalo en las 2 horas siguientes al entreno si no vas a comer pronto.",
            "Proteína + carbohidratos para reponer glucógeno y empezar a reparar músculo."),
        MealIdea("post_skyr", "Skyr con cereales y mango", Meal.POST_WORKOUT, ALL, 3,
            listOf("food_skyr" to 250.0, "food_cornflakes" to 40.0, "food_mango" to 150.0),
            "Mezcla el skyr con el mango troceado y añade los cereales justo antes de comer para que crujan.",
            "Fresco, rápido y con unos 30 g de proteína."),
        // ---- Meriendas ----
        MealIdea("quark_pb", "Queso batido con fresas y cacahuete", Meal.SNACK, ALL, 3,
            listOf("food_quark" to 250.0, "food_strawberries" to 150.0, "food_peanut_butter" to 16.0),
            "Mezcla el queso batido con la crema de cacahuete y añade las fresas troceadas. Si te gusta dulce, edulcorante o canela.",
            "Sabe a postre y tiene 25 g de proteína. También es perfecto antes de dormir."),
        MealIdea("tuna_sandwich", "Bocadillo de atún y tomate", Meal.SNACK, BULK, 5,
            listOf("food_bread_white" to 100.0, "food_tuna_oil" to 56.0, "food_tomato" to 60.0, "food_olive_oil" to 5.0),
            "Abre el pan, unta tomate rallado con aceite y sal y añade el atún escurrido.",
            "Merienda de toda la vida que sigue siendo de lo mejor para ganar músculo."),
        MealIdea("fruit_nuts", "Fruta y puñado de almendras", Meal.SNACK, ALL, 1,
            listOf("food_apple" to 180.0, "food_almonds" to 25.0, "food_yogurt" to 125.0),
            "Una pieza de fruta, un yogur y un puñado de almendras. Cabe en la mochila del gym.",
            "Snack saciante con fibra, grasas buenas y algo de proteína."),
    )

    fun ideaMacros(idea: MealIdea): Macros = idea.ingredients.fold(Macros()) { acc, (id, g) ->
        acc + (FoodDatabase.byId(id)?.macrosFor(g) ?: Macros())
    }
}
