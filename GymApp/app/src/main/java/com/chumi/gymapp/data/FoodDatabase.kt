package com.chumi.gymapp.data

import com.chumi.gymapp.data.FoodCategory.CARBS
import com.chumi.gymapp.data.FoodCategory.DAIRY
import com.chumi.gymapp.data.FoodCategory.DRINKS
import com.chumi.gymapp.data.FoodCategory.FATS
import com.chumi.gymapp.data.FoodCategory.FRUIT
import com.chumi.gymapp.data.FoodCategory.LEGUMES
import com.chumi.gymapp.data.FoodCategory.OTHER
import com.chumi.gymapp.data.FoodCategory.PROTEIN
import com.chumi.gymapp.data.FoodCategory.SUPPLEMENTS
import com.chumi.gymapp.data.FoodCategory.VEG

/**
 * Base de alimentos habituales. Valores aproximados por 100 g (tablas BEDCA / USDA).
 * Se indica si el alimento está crudo o cocinado, porque cambia mucho el peso.
 */
object FoodDatabase {

    private fun f(
        id: String, name: String, cat: FoodCategory,
        kcal: Double, p: Double, c: Double, fat: Double, fiber: Double = 0.0,
        serving: Double = 100.0, servingName: String = "ración",
    ) = Food("food_$id", name, cat, kcal, p, c, fat, fiber, serving, servingName)

    val foods: List<Food> = listOf(
        // ---- Carnes, pescados y huevos ----
        f("chicken_raw", "Pechuga de pollo (cruda)", PROTEIN, 110.0, 23.0, 0.0, 1.5, serving = 150.0, servingName = "filete"),
        f("chicken_grill", "Pechuga de pollo (a la plancha)", PROTEIN, 165.0, 31.0, 0.0, 3.6, serving = 120.0, servingName = "filete"),
        f("chicken_thigh", "Muslo de pollo sin piel (cocinado)", PROTEIN, 180.0, 25.0, 0.0, 8.5, serving = 120.0, servingName = "ración"),
        f("turkey_breast", "Pavo (pechuga, cruda)", PROTEIN, 105.0, 24.0, 0.0, 1.0, serving = 150.0, servingName = "filete"),
        f("turkey_cold", "Fiambre de pechuga de pavo", PROTEIN, 105.0, 19.0, 2.0, 2.0, serving = 15.0, servingName = "loncha"),
        f("beef_lean", "Ternera magra (cruda)", PROTEIN, 130.0, 21.0, 0.0, 5.0, serving = 150.0, servingName = "filete"),
        f("beef_mince5", "Carne picada de vacuno 5% grasa (cruda)", PROTEIN, 125.0, 21.0, 0.0, 5.0, serving = 125.0),
        f("beef_mince15", "Carne picada mixta 15% grasa (cruda)", PROTEIN, 215.0, 18.0, 0.0, 15.0, serving = 125.0),
        f("pork_loin", "Lomo de cerdo (crudo)", PROTEIN, 140.0, 21.0, 0.0, 6.0, serving = 150.0, servingName = "filete"),
        f("ham_serrano", "Jamón serrano", PROTEIN, 240.0, 31.0, 0.0, 13.0, serving = 15.0, servingName = "loncha"),
        f("ham_cooked", "Jamón cocido / york", PROTEIN, 110.0, 18.0, 1.5, 3.5, serving = 15.0, servingName = "loncha"),
        f("salmon", "Salmón (crudo)", PROTEIN, 208.0, 20.0, 0.0, 13.0, serving = 150.0, servingName = "lomo"),
        f("tuna_water", "Atún al natural (lata, escurrido)", PROTEIN, 110.0, 25.0, 0.0, 1.0, serving = 56.0, servingName = "lata"),
        f("tuna_oil", "Atún en aceite (lata, escurrido)", PROTEIN, 190.0, 26.0, 0.0, 9.0, serving = 56.0, servingName = "lata"),
        f("hake", "Merluza (cruda)", PROTEIN, 75.0, 17.0, 0.0, 0.8, serving = 150.0, servingName = "filete"),
        f("cod", "Bacalao fresco (crudo)", PROTEIN, 80.0, 18.0, 0.0, 0.7, serving = 150.0, servingName = "lomo"),
        f("prawns", "Gambas / langostinos (pelados)", PROTEIN, 85.0, 19.0, 0.0, 1.0, serving = 100.0),
        f("sardines", "Sardinas en lata", PROTEIN, 210.0, 24.0, 0.0, 12.0, serving = 85.0, servingName = "lata"),
        f("egg", "Huevo entero", PROTEIN, 143.0, 12.6, 0.7, 9.5, serving = 55.0, servingName = "huevo L"),
        f("egg_white", "Claras de huevo", PROTEIN, 52.0, 11.0, 0.7, 0.2, serving = 33.0, servingName = "clara"),
        f("tofu", "Tofu firme", PROTEIN, 120.0, 13.0, 2.0, 7.0, 1.0, serving = 100.0),
        f("seitan", "Seitán", PROTEIN, 120.0, 22.0, 4.0, 2.0, serving = 100.0),
        // ---- Lácteos ----
        f("milk_skim", "Leche desnatada", DAIRY, 35.0, 3.4, 4.9, 0.1, serving = 250.0, servingName = "vaso"),
        f("milk_semi", "Leche semidesnatada", DAIRY, 46.0, 3.2, 4.8, 1.6, serving = 250.0, servingName = "vaso"),
        f("milk_whole", "Leche entera", DAIRY, 64.0, 3.1, 4.7, 3.6, serving = 250.0, servingName = "vaso"),
        f("oat_drink", "Bebida de avena", DAIRY, 45.0, 1.0, 7.0, 1.5, 0.8, serving = 250.0, servingName = "vaso"),
        f("soy_drink", "Bebida de soja sin azúcar", DAIRY, 33.0, 3.3, 0.5, 1.8, 0.5, serving = 250.0, servingName = "vaso"),
        f("yogurt", "Yogur natural", DAIRY, 61.0, 3.5, 4.7, 3.3, serving = 125.0, servingName = "yogur"),
        f("yogurt_greek", "Yogur griego natural", DAIRY, 130.0, 3.5, 4.0, 10.0, serving = 125.0, servingName = "yogur"),
        f("skyr", "Yogur proteico / skyr natural", DAIRY, 63.0, 11.0, 4.0, 0.2, serving = 150.0, servingName = "tarrina"),
        f("quark", "Queso fresco batido 0%", DAIRY, 46.0, 8.0, 3.5, 0.2, serving = 125.0, servingName = "tarrina"),
        f("cottage", "Queso cottage", DAIRY, 98.0, 11.0, 3.4, 4.3, serving = 100.0),
        f("burgos", "Queso fresco tipo Burgos", DAIRY, 175.0, 12.0, 3.0, 13.0, serving = 60.0, servingName = "porción"),
        f("burgos_light", "Queso fresco tipo Burgos 0%", DAIRY, 70.0, 12.0, 4.0, 0.5, serving = 60.0, servingName = "porción"),
        f("cheese_cured", "Queso curado", DAIRY, 400.0, 26.0, 0.5, 33.0, serving = 30.0, servingName = "cuña"),
        f("mozzarella", "Mozzarella", DAIRY, 250.0, 18.0, 2.0, 19.0, serving = 125.0, servingName = "bola"),
        // ---- Cereales, pan y tubérculos ----
        f("rice_raw", "Arroz blanco (crudo)", CARBS, 355.0, 7.0, 79.0, 0.6, 1.0, serving = 80.0, servingName = "ración"),
        f("rice_cooked", "Arroz blanco (cocido)", CARBS, 130.0, 2.7, 28.0, 0.3, 0.4, serving = 200.0, servingName = "plato"),
        f("rice_brown", "Arroz integral (crudo)", CARBS, 350.0, 7.5, 73.0, 2.7, 3.5, serving = 80.0, servingName = "ración"),
        f("pasta_raw", "Pasta (cruda)", CARBS, 355.0, 12.5, 71.0, 1.5, 3.0, serving = 80.0, servingName = "ración"),
        f("pasta_cooked", "Pasta (cocida)", CARBS, 158.0, 5.8, 31.0, 0.9, 1.8, serving = 200.0, servingName = "plato"),
        f("pasta_whole", "Pasta integral (cruda)", CARBS, 340.0, 13.0, 64.0, 2.5, 8.0, serving = 80.0, servingName = "ración"),
        f("oats", "Copos de avena", CARBS, 375.0, 13.0, 60.0, 7.0, 10.0, serving = 50.0, servingName = "ración"),
        f("cream_rice", "Crema de arroz", CARBS, 370.0, 7.0, 82.0, 1.0, 1.0, serving = 50.0, servingName = "ración"),
        f("bread_white", "Pan blanco (barra)", CARBS, 277.0, 9.0, 55.0, 1.6, 2.5, serving = 60.0, servingName = "trozo"),
        f("bread_whole", "Pan integral", CARBS, 250.0, 10.0, 43.0, 3.5, 7.0, serving = 30.0, servingName = "rebanada"),
        f("bread_sliced", "Pan de molde", CARBS, 265.0, 8.0, 48.0, 4.0, 3.0, serving = 25.0, servingName = "rebanada"),
        f("rice_cakes", "Tortitas de arroz", CARBS, 385.0, 8.0, 81.0, 2.8, 3.0, serving = 8.0, servingName = "tortita"),
        f("wrap", "Tortilla de trigo (wrap)", CARBS, 310.0, 8.5, 52.0, 7.5, 3.0, serving = 60.0, servingName = "tortilla"),
        f("cornflakes", "Copos de maíz (cereales)", CARBS, 375.0, 7.0, 84.0, 0.9, 3.0, serving = 40.0, servingName = "bol"),
        f("muesli", "Muesli sin azúcar", CARBS, 365.0, 10.0, 64.0, 6.0, 8.0, serving = 50.0, servingName = "bol"),
        f("potato", "Patata (cruda)", CARBS, 77.0, 2.0, 17.0, 0.1, 2.2, serving = 200.0, servingName = "patata mediana"),
        f("potato_boiled", "Patata cocida", CARBS, 86.0, 1.7, 20.0, 0.1, 1.8, serving = 200.0, servingName = "ración"),
        f("sweet_potato", "Boniato (crudo)", CARBS, 86.0, 1.6, 20.0, 0.1, 3.0, serving = 200.0, servingName = "boniato mediano"),
        f("quinoa", "Quinoa (cruda)", CARBS, 368.0, 14.0, 64.0, 6.0, 7.0, serving = 70.0, servingName = "ración"),
        f("couscous", "Cuscús (crudo)", CARBS, 376.0, 13.0, 77.0, 0.6, 5.0, serving = 70.0, servingName = "ración"),
        f("honey", "Miel", CARBS, 304.0, 0.3, 82.0, 0.0, serving = 20.0, servingName = "cucharada"),
        f("sugar", "Azúcar", CARBS, 400.0, 0.0, 100.0, 0.0, serving = 8.0, servingName = "sobre"),
        f("jam", "Mermelada", CARBS, 250.0, 0.4, 60.0, 0.0, serving = 20.0, servingName = "cucharada"),
        // ---- Legumbres ----
        f("lentils", "Lentejas (cocidas)", LEGUMES, 116.0, 9.0, 20.0, 0.4, 8.0, serving = 200.0, servingName = "plato"),
        f("chickpeas", "Garbanzos (cocidos)", LEGUMES, 130.0, 7.0, 17.0, 2.5, 7.0, serving = 200.0, servingName = "plato"),
        f("beans", "Alubias (cocidas)", LEGUMES, 127.0, 8.7, 22.8, 0.5, 7.0, serving = 200.0, servingName = "plato"),
        f("peas", "Guisantes", LEGUMES, 81.0, 5.4, 14.0, 0.4, 5.0, serving = 100.0),
        f("edamame", "Edamame", LEGUMES, 121.0, 12.0, 9.0, 5.0, 5.0, serving = 100.0),
        f("hummus", "Hummus", LEGUMES, 166.0, 7.9, 14.3, 9.6, 6.0, serving = 30.0, servingName = "cucharada"),
        // ---- Frutas ----
        f("banana", "Plátano", FRUIT, 89.0, 1.1, 23.0, 0.3, 2.6, serving = 120.0, servingName = "plátano"),
        f("apple", "Manzana", FRUIT, 52.0, 0.3, 14.0, 0.2, 2.4, serving = 180.0, servingName = "manzana"),
        f("orange", "Naranja", FRUIT, 47.0, 0.9, 12.0, 0.1, 2.4, serving = 180.0, servingName = "naranja"),
        f("strawberries", "Fresas", FRUIT, 32.0, 0.7, 7.7, 0.3, 2.0, serving = 150.0, servingName = "bol"),
        f("blueberries", "Arándanos", FRUIT, 57.0, 0.7, 14.0, 0.3, 2.4, serving = 100.0, servingName = "puñado grande"),
        f("grapes", "Uvas", FRUIT, 69.0, 0.7, 18.0, 0.2, 0.9, serving = 150.0, servingName = "racimo"),
        f("pineapple", "Piña", FRUIT, 50.0, 0.5, 13.0, 0.1, 1.4, serving = 150.0, servingName = "rodajas"),
        f("kiwi", "Kiwi", FRUIT, 61.0, 1.1, 15.0, 0.5, 3.0, serving = 75.0, servingName = "kiwi"),
        f("pear", "Pera", FRUIT, 57.0, 0.4, 15.0, 0.1, 3.1, serving = 170.0, servingName = "pera"),
        f("watermelon", "Sandía", FRUIT, 30.0, 0.6, 7.6, 0.2, 0.4, serving = 300.0, servingName = "tajada"),
        f("melon", "Melón", FRUIT, 34.0, 0.8, 8.2, 0.2, 0.9, serving = 300.0, servingName = "tajada"),
        f("mango", "Mango", FRUIT, 60.0, 0.8, 15.0, 0.4, 1.6, serving = 200.0, servingName = "mango"),
        f("dates", "Dátiles", FRUIT, 282.0, 2.5, 75.0, 0.4, 8.0, serving = 10.0, servingName = "dátil"),
        // ---- Verduras ----
        f("broccoli", "Brócoli", VEG, 34.0, 2.8, 7.0, 0.4, 2.6, serving = 150.0),
        f("spinach", "Espinacas", VEG, 23.0, 2.9, 3.6, 0.4, 2.2, serving = 100.0),
        f("lettuce", "Lechuga / mezcla de hojas", VEG, 15.0, 1.4, 2.9, 0.2, 1.3, serving = 100.0, servingName = "bol"),
        f("tomato", "Tomate", VEG, 18.0, 0.9, 3.9, 0.2, 1.2, serving = 150.0, servingName = "tomate"),
        f("carrot", "Zanahoria", VEG, 41.0, 0.9, 10.0, 0.2, 2.8, serving = 80.0, servingName = "zanahoria"),
        f("zucchini", "Calabacín", VEG, 17.0, 1.2, 3.1, 0.3, 1.0, serving = 200.0, servingName = "calabacín"),
        f("pepper", "Pimiento", VEG, 31.0, 1.0, 6.0, 0.3, 2.1, serving = 150.0, servingName = "pimiento"),
        f("onion", "Cebolla", VEG, 40.0, 1.1, 9.3, 0.1, 1.7, serving = 100.0, servingName = "cebolla"),
        f("mushrooms", "Champiñones", VEG, 22.0, 3.1, 3.3, 0.3, 1.0, serving = 100.0),
        f("green_beans", "Judías verdes", VEG, 31.0, 1.8, 7.0, 0.2, 2.7, serving = 150.0),
        f("cucumber", "Pepino", VEG, 15.0, 0.7, 3.6, 0.1, 0.5, serving = 150.0, servingName = "pepino"),
        f("asparagus", "Espárragos trigueros", VEG, 20.0, 2.2, 3.9, 0.1, 2.1, serving = 100.0, servingName = "manojo"),
        f("cauliflower", "Coliflor", VEG, 25.0, 1.9, 5.0, 0.3, 2.0, serving = 150.0),
        f("eggplant", "Berenjena", VEG, 25.0, 1.0, 6.0, 0.2, 3.0, serving = 150.0),
        f("gazpacho", "Gazpacho", VEG, 45.0, 1.0, 5.0, 2.5, 1.0, serving = 250.0, servingName = "vaso"),
        // ---- Grasas y frutos secos ----
        f("olive_oil", "Aceite de oliva virgen extra", FATS, 884.0, 0.0, 0.0, 100.0, serving = 10.0, servingName = "cucharada"),
        f("butter", "Mantequilla", FATS, 717.0, 0.9, 0.1, 81.0, serving = 10.0, servingName = "porción"),
        f("avocado", "Aguacate", FATS, 160.0, 2.0, 8.5, 14.7, 6.7, serving = 80.0, servingName = "medio aguacate"),
        f("almonds", "Almendras", FATS, 579.0, 21.0, 22.0, 50.0, 12.5, serving = 25.0, servingName = "puñado"),
        f("walnuts", "Nueces", FATS, 654.0, 15.0, 14.0, 65.0, 6.7, serving = 25.0, servingName = "puñado"),
        f("peanuts", "Cacahuetes", FATS, 567.0, 26.0, 16.0, 49.0, 8.5, serving = 25.0, servingName = "puñado"),
        f("peanut_butter", "Crema de cacahuete 100%", FATS, 588.0, 25.0, 20.0, 50.0, 6.0, serving = 16.0, servingName = "cucharada"),
        f("cashews", "Anacardos", FATS, 553.0, 18.0, 30.0, 44.0, 3.3, serving = 25.0, servingName = "puñado"),
        f("olives", "Aceitunas", FATS, 145.0, 1.0, 4.0, 15.0, 3.3, serving = 30.0, servingName = "puñado"),
        f("dark_choc", "Chocolate negro 85%", FATS, 600.0, 10.0, 19.0, 50.0, 12.0, serving = 10.0, servingName = "onza"),
        f("chia", "Semillas de chía", FATS, 486.0, 17.0, 42.0, 31.0, 34.0, serving = 15.0, servingName = "cucharada"),
        // ---- Suplementos ----
        f("whey", "Proteína whey (polvo)", SUPPLEMENTS, 380.0, 78.0, 7.0, 5.0, serving = 30.0, servingName = "cacito"),
        f("whey_iso", "Proteína isolate (polvo)", SUPPLEMENTS, 370.0, 88.0, 2.0, 1.0, serving = 30.0, servingName = "cacito"),
        f("casein", "Caseína (polvo)", SUPPLEMENTS, 360.0, 80.0, 6.0, 2.0, serving = 30.0, servingName = "cacito"),
        f("creatine", "Creatina monohidrato", SUPPLEMENTS, 0.0, 0.0, 0.0, 0.0, serving = 5.0, servingName = "dosis"),
        f("protein_bar", "Barrita de proteína", SUPPLEMENTS, 340.0, 33.0, 33.0, 12.0, 5.0, serving = 60.0, servingName = "barrita"),
        f("gainer", "Ganador de peso (polvo)", SUPPLEMENTS, 380.0, 20.0, 70.0, 3.0, serving = 100.0, servingName = "dosis"),
        // ---- Bebidas ----
        f("coffee", "Café solo", DRINKS, 2.0, 0.1, 0.0, 0.0, serving = 60.0, servingName = "taza"),
        f("orange_juice", "Zumo de naranja", DRINKS, 45.0, 0.7, 10.4, 0.2, 0.2, serving = 250.0, servingName = "vaso"),
        f("cola", "Refresco de cola", DRINKS, 42.0, 0.0, 10.6, 0.0, serving = 330.0, servingName = "lata"),
        f("cola_zero", "Refresco zero", DRINKS, 0.3, 0.0, 0.0, 0.0, serving = 330.0, servingName = "lata"),
        f("beer", "Cerveza", DRINKS, 43.0, 0.5, 3.6, 0.0, serving = 330.0, servingName = "tercio"),
        f("wine", "Vino tinto", DRINKS, 85.0, 0.1, 2.6, 0.0, serving = 150.0, servingName = "copa"),
        f("isotonic", "Bebida isotónica", DRINKS, 24.0, 0.0, 6.0, 0.0, serving = 500.0, servingName = "botella"),
        // ---- Otros y platos ----
        f("tortilla_esp", "Tortilla de patatas", OTHER, 190.0, 6.5, 13.0, 12.0, 1.0, serving = 150.0, servingName = "pincho"),
        f("pizza", "Pizza", OTHER, 266.0, 11.0, 33.0, 10.0, 2.3, serving = 100.0, servingName = "porción"),
        f("burger", "Hamburguesa completa (fast food)", OTHER, 254.0, 13.0, 30.0, 9.0, 1.5, serving = 200.0, servingName = "hamburguesa"),
        f("fries", "Patatas fritas (de bolsa)", OTHER, 536.0, 7.0, 53.0, 34.0, 4.0, serving = 30.0, servingName = "bolsita"),
        f("cookies", "Galletas tipo María", OTHER, 436.0, 7.0, 74.0, 12.0, 2.0, serving = 6.0, servingName = "galleta"),
        f("croissant", "Cruasán", OTHER, 406.0, 8.0, 46.0, 21.0, 2.0, serving = 60.0, servingName = "cruasán"),
        f("tomato_sauce", "Tomate frito", OTHER, 75.0, 1.5, 10.0, 3.0, 1.5, serving = 50.0, servingName = "ración"),
        f("ketchup", "Ketchup", OTHER, 112.0, 1.0, 26.0, 0.1, serving = 15.0, servingName = "cucharada"),
        f("mayo", "Mayonesa", OTHER, 680.0, 1.0, 1.0, 75.0, serving = 15.0, servingName = "cucharada"),
    )

    fun byId(id: String): Food? = foods.find { it.id == id }
}
