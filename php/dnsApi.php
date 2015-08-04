<?php

$main = array();
$main["ServerTime"] = "";         //текущее время на сервере, будет необходимо при последующих запросах последней информации

/* Города */

$city = array();
//город №1
$city["n0"]["Id"]             = "";     //Идентификатор, здесь может быть вообще ваш цифровой Id города в базе
$city["n0"]["Name"]           = "";     //Название
$city["n0"]["ReferencePhone"] = ""; //Телефон главной справочной службы, обязательно в формате (код) телефон через тире, и если номера нет, то указать номер 8-800
$city["n0"]["SupportPhone"]   = ""; //телефон технической поддержки, или СЦ. если нет, то отдавать пустое значение.
$city["n0"]["Latitude"]       = "";       //Широта
$city["n0"]["Longitude"]      = "";       //Долгота
$city["n0"]["ShopsCount"]     = "";
$city["n0"]["Enabled"]        = true;

$main["Cityes"] = $city;

/* Магазины */

$shop= array();
//магазин №1
$shop["n0"]["Name"]         = "";                 //Название
$shop["n0"]["Address"]      = "";            //Адрес
$shop["n0"]["Phone"]        = "";                    //Телефон
$shop["n0"]["WorkTime"]     = "";   //Время работы
$shop["n0"]["Latitude"]     = "";                          //Широта
$shop["n0"]["Longitude"]    = "";
$shop["n0"]["Enabled"]      = true;

$main["Shops"] = $shop;

/* Категории */

$category= array();
//категория №1
$category["n0"]["Id"]           = "";                                //Идентификатор категории
$category["n0"]["Name"]         = "";                     //Наименование
$category["n0"]["IdParent"]     = "";                                  //Идентификатор родительской категории. Если родительская категория root, то значение должно быть 0
$category["n0"]["Enabled"]      = true;                                 //false если категория к примеру удалена. тогда она удалится из кэша клиента

$category["n0"]["Filters"]["n0"]["Type"]    = "input/check/select";                  //Тип фильтра, данный тип означает поле ввода
$category["n0"]["Filters"]["n0"]["Id"]      = "";                  //Идентификатор фильтра
$category["n0"]["Filters"]["n0"]["Name"]    = "";                   //Наименовение фильтра
$category["n0"]["Filters"]["n0"]["Values"]["n0"]["Id"]      = "";            //Id значения №1
$category["n0"]["Filters"]["n0"]["Values"]["n0"]["Name"]    = "";    //Наименование значения №1

$category["n0"]["FiltersFull"]["n0"]["Type"]    = "input/check/select";                  //Тип фильтра, данный тип означает поле ввода
$category["n0"]["FiltersFull"]["n0"]["Id"]      = "";                  //Идентификатор фильтра
$category["n0"]["FiltersFull"]["n0"]["Name"]    = "";                   //Наименовение фильтра
$category["n0"]["FiltersFull"]["n0"]["Values"]["n0"]["Id"]      = "";            //Id значения №1
$category["n0"]["FiltersFull"]["n0"]["Values"]["n0"]["Name"]    = "";    //Наименование значения №1

$main["Category"] = $category;

/* Товары */

$items= array();

$items["n0"]["Id"]          = "";       //Идентификатор товара
$items["n0"]["Name"]        = "";    //Наименование
$items["n0"]["Price"]       = "";
$items["n0"]["Grade"]       = "";
$items["n0"]["Comments"]    = "";
$items["n0"]["Enabled"]     = true;           //false если товар к примеру удален. тогда он удалится из кэша клиента

$main["Items"] = $items;

/* Товар */

$item= array();

$item["n0"]["Id"]          = "";
$item["n0"]["Name"]        = "";    //Наименование
$item["n0"]["Price"]       = "";
$item["n0"]["Grade"]       = "";
$item["n0"]["Description"] = "";
$item["n0"]["Images"]["n0"]["Link"]    = "";
$item["n0"]["Features"]["n0"]["Name"]  = "";
$item["n0"]["Features"]["n0"]["Values"]["n0"]["Key"]   = "";
$item["n0"]["Features"]["n0"]["Values"]["n0"]["Value"] = "";

$item["n0"]["Grades"]["n0"]["Plus"]    = "";
$item["n0"]["Grades"]["n0"]["Minus"]   = "";
$item["n0"]["Grades"]["n0"]["Comment"] = "";
$item["n0"]["Grades"]["n0"]["Grade"]   = "";
$item["n0"]["Grades"]["n0"]["User"]    = "";
$item["n0"]["Grades"]["n0"]["City"]    = "";
$item["n0"]["Grades"]["n0"]["Date"]    = "";

$item["n0"]["Comments"]["n0"]["Id"]        = "";
$item["n0"]["Comments"]["n0"]["IdParent"]  = "";
$item["n0"]["Comments"]["n0"]["Text"]      = "";
$item["n0"]["Comments"]["n0"]["IdParent"]  = "";
$item["n0"]["Comments"]["n0"]["User"]      = "";
$item["n0"]["Comments"]["n0"]["City"]      = "";
$item["n0"]["Comments"]["n0"]["Date"]      = "";

$main["Item"] = $item;

/* Поиск */

$search= array();

$search["Type"] = "error/item/items";

$search["ErrorMessage"] = "";
$search["IdItem"]       = "";

$search["Items"]["n0"]["Id"]          = "";    //Идентификатор товара
$search["Items"]["n0"]["Name"]        = "";    //Наименование
$search["Items"]["n0"]["Price"]       = "";
$search["Items"]["n0"]["Grade"]       = "";
$search["Items"]["n0"]["Comments"]    = "";
$search["Items"]["n0"]["Enabled"]     = true;

$main["Search"] = $search;

/* Топ 10 */

$top10 = array();

$top10["n0"]["Id"]          = "";    //Идентификатор товара
$top10["n0"]["Name"]        = "";    //Наименование
$top10["n0"]["Price"]       = "";
$top10["n0"]["Image"]       = "";
$top10["n0"]["Enabled"]     = true;

$main["Top10"] = $top10;

/* Баннер на витрину */

$banner = array();

$banner["Image"]       = "";

$main["Banner"] = $banner;

echo json_encode($main);

exit();