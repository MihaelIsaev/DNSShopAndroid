<?php

include('gzdoc.php'); 

$what           = $_GET['what'];
$city           = $_GET['city'];
$id             = $_GET['id'];
$vendor         = $_GET['vendor'];
$search         = $_GET['search'];

switch($what){
    case "vendors":
        echo getVendors($city, $id);
        break;    
    case "items":
        echo getItems($city, $id, $vendor);
        break;
    case "item":
        echo getItem($city, $id);
        break;
    case "shops":
        echo getShops($city);
        break;
    case "search":
        echo getSearch($city, $search);
        break;
}

function getFilters($xpath){
    $result = array();
    
    $filter = array();
    
    //filter control_1 - селект
    //filter control_2 - один чекбокс
    //filter control_3 - несколько чекбоксов
    //filter control_4 - два инпута
    
    
    $filters = $xpath->query('//div[@class="cnt"]/form[@id="catalog_filter"]/ul/li');
    
    $i = 0;
    foreach($filters as $inner) {
        $domTmp = new DOMDocument();
        $domTmp->appendChild($domTmp->importNode($filters->item($i), true));
        $xpathTmp = new DOMXPath($domTmp);
        
        $type = $inner->getAttribute("class");
        
        if($type == "filter control_1"){     //селект
            $result["n".$i]["Type"] = "select";
            $label = $xpathTmp->query('//label');
            $result["n".$i]["Label"] = $label->item(0)->nodeValue;
            $key = $xpathTmp->query('//select');
            $result["n".$i]["Key"] = $key->item(0)->getAttribute("id");
            $option = $xpathTmp->query('//option');
            $n=0;
            foreach($option as $optionI) {
                $result["n".$i]["Values"]["n".$n]["Name"] = $optionI->nodeValue;
                $result["n".$i]["Values"]["n".$n]["Value"] = $optionI->getAttribute("value");
                $n++;
            }
        }
        elseif($type == "filter control_2"){ //один чекбокс
            $result["n".$i]["Type"] = "one_checkbox";
            $label = $xpathTmp->query('//label');
            $result["n".$i]["Label"] = $label->item(0)->nodeValue;
            $input = $xpathTmp->query('//input');
            $result["n".$i]["Key"] = $input->item(0)->getAttribute("id");
                        
        }
        elseif($type == "filter control_3"){ //несколько чекбоксов
            $result["n".$i]["Type"] = "multi_checkbox";
            $label = $xpathTmp->query('//label');
            $result["n".$i]["Label"] = $label->item(0)->nodeValue;
            $input = $xpathTmp->query('//input');
            $n=0;
            foreach($input as $innerI) {
                $result["n".$i]["Values"]["n".$n]["Name"] = $innerI->getAttribute("id");
                $result["n".$i]["Values"]["n".$n]["Key"] = $label->item($n+1)->nodeValue;                
                $n++;
            }
            
        }
        elseif($type == "filter control_4"){ //два инпута
            $result["n".$i]["Type"] = "two_inputs";
            $label = $xpathTmp->query('//label');
            $result["n".$i]["Label"] = $label->item(0)->nodeValue;
            $input = $xpathTmp->query('//input');
            $n=0;
            foreach($input as $innerI) {
                if($n==0)
                    $result["n".$i]["From"] = $innerI->getAttribute("id");
                elseif($n==1)
                    $result["n".$i]["To"] = $innerI->getAttribute("id");
                $n++;
            }
        }
        
        $i++;
        //$filter[]=$inner->nodeValue;$vend[]=$inner->getAttribute("for");
        
    }
    
    
    
    
    return $result;
}

function getVendors($city, $id){
    $result = array();
    $curl = curl_init();
    
    $vendor   = array();
    $vend     = array();
    $code     = array();
    $name     = array();
    $price    = array();
    
    $html = getItemsHtml($curl, $city, $id);
    $dom = new domDocument;
    @$dom->loadHTML($html); 
    $xpath = new DOMXPath($dom);

    $filter = $xpath->query('//li[@class="filter control_3"]');
    
    $docv = new DOMDocument();
    $docv->appendChild($docv->importNode($filter->item(0), true));
    $xpathv = new DOMXPath($docv);
    $vendors = $xpathv->query('//ul/li/label');foreach($vendors as $inner) {$vendor[]=$inner->nodeValue;$vend[]=$inner->getAttribute("for");}
    
    if(count($vendor)>1){
        for($v=0;$v<count($vendor);$v++){
            $code     = array();
            $name     = array();
            $price    = array();
            $result["t$v"]['VendorId'] = $vend[$v];
            $result["t$v"]['Vendor'] = $vendor[$v];
        }
    }else{
        $items = $xpath->query('//tr[@itemscope]');
        $itemsCode = $xpath->query('//tr[@itemscope]/td[@class="c"]');
        foreach($itemsCode as $inner) {
            $code[]     =$inner->nodeValue;
        }
        $itemsName = $xpath->query('//tr[@itemscope]/td[@class="t"]/a/span[@itemprop="name"]');foreach($itemsName as $inner) {$name[]=$inner->nodeValue;}
        $itemsPrice = $xpath->query('//tr[@itemscope]/td[@class="p"]/div');foreach($itemsPrice as $inner) {$price[]=$inner->nodeValue;}
        $i=0;
        foreach($items as $inner) {
            $pr = $price[$i];
            $result["t$i"] = array('Code'=>$code[$i], 'Name'=>$name[$i], 'Price'=>$pr);
            $i++;
        }
    }
    curl_close($curl);
    
    return json_encode($result);
}

function getItems($city, $id, $vendorID){
    $result = array();
    $curl = curl_init();
    
    $code     = array();
    $name     = array();
    $price    = array();
    
    $html = getItemsHtmlByVendor($curl, $city, $id, $vendorID);
    $dom = new domDocument;
    @$dom->loadHTML($html); 
    $xpath = new DOMXPath($dom);
    $items = $xpath->query('//tr[@itemscope]');
    $itemsCode = $xpath->query('//tr[@itemscope]/td[@class="c"]');
    foreach($itemsCode as $inner) {
        $code[]     =$inner->nodeValue;
    }
    $itemsName = $xpath->query('//tr[@itemscope]/td[@class="t"]/a/span[@itemprop="name"]');foreach($itemsName as $inner) {$name[]=$inner->nodeValue;}
    $itemsPrice = $xpath->query('//tr[@itemscope]/td[@class="p"]/div');
    foreach($itemsPrice as $inner) {if($inner->getAttribute("class")!=="prev")$price[]=$inner->nodeValue;}
    $i=0;
    foreach($items as $inner) {
        $pr = $price[$i];
        $result["t$i"] = array('Code'=>$code[$i], 'Name'=>$name[$i], 'Price'=>$pr);
        $i++;
    }
    curl_close($curl);
    
    $result["Filters"] = getFilters($xpath);
    
    return json_encode($result);
}

function getItem($city, $id){
    $print = array();

    $curl = curl_init();

    $avail = array();
    $grade = array();
    $review = array();
    $comment = array();
    $reviewUser = array();
    $reviewCity = array();
    $reviewDate = array();
    $commentUser = array();
    $commentCity = array();
    $commentDate = array();
    $html = getItemHtml($curl, $city, $id);
    $dom = new domDocument;
    @$dom->loadHTML($html); 
    $xpath = new DOMXPath($dom);

    //Название
    $name = $xpath->query('//h1[@itemprop="name"]');
    //Нормальная цена
    $price = $xpath->query('//li[@class="price"]/div[@class="norm"]/span[@class="value"]');
    
    //Код
    $code = $xpath->query('//li[@class="code"]/span');
    //Картинка
    $img = $xpath->query('//img[@id="main_ill_img"]');
    //Цена со скидкой, какбы новая
    $priceNew = $xpath->query('//li[@class="price"]/div[@class="new"]/span[@class="value"]');
    //Описание
    $descr = $xpath->query('//div[@itemprop="description"]');
    //Спец
    $spec = $xpath->query('//div[@id="catalog_item_options"]');

    
    if($price->item(0)->nodeValue == "")
        $pr = $priceNew->item(0)->nodeValue;
    else
        $pr = $price->item(0)->nodeValue;

    $doc2 = new DOMDocument();
    $doc2->appendChild($doc2->importNode($spec->item(0), true));
    $priceSheetHtml = $doc2->saveHTML();

    $print['Name']        = $name->item(0)->nodeValue;
    $print['Code']        = "Код: ".$code->item(0)->nodeValue;
    $print['Image']       = $img->item(0)->getAttribute(src);
    $print['Price']       = $pr;
    $print['Description'] = $descr->item(0)->nodeValue;
    //$print['Features']    = str_replace("Показать только основные",  "", $priceSheetHtml);

    //Наличие
    $avails = $xpath->query('//div[@class="avail"]/ul/li/a');foreach($avails as $inner) {$avail[]=$inner->nodeValue;}
    
    $av = "";
    for($i=0;$i<count($avail);$i++)
        $av .= " ".$avail[$i];
    $print['Availability'] = "Наличие: ".$av;
    
    //Отзывы
    $reviews = $xpath->query('//div[@id="opinion_list"]/ul/li/div[@class="text"]');foreach($reviews as $inner) {$review[]=$inner->nodeValue;}

    //Оценки
    $grades = $xpath->query('//div[@id="opinion_list"]/ul/li/div/div[@class="grade"]');foreach($grades as $inner) {$grade[]=$inner->nodeValue;}
    
    $reviewsUser = $xpath->query('//div[@id="opinion_list"]/ul/li/div[@class="last_block"]/p[@class="stamp"]/span[@class="user category0"]');foreach($reviewsUser as $inner) {$reviewUser[]=$inner->nodeValue;}
    $reviewsCity = $xpath->query('//div[@id="opinion_list"]/ul/li/div[@class="last_block"]/p[@class="stamp"]/span[@class="city"]');foreach($reviewsCity as $inner) {$reviewCity[]=$inner->nodeValue;}
    $reviewsDate = $xpath->query('//div[@id="opinion_list"]/ul/li/div[@class="last_block"]/p[@class="stamp"]/span[@class="date"]');foreach($reviewsDate as $inner) {$reviewDate[]=$inner->nodeValue;}

    //комментарии
    $comments = $xpath->query('//div[@class="box"]/p');foreach($comments as $inner) {$comment[]=$inner->nodeValue;}
    $commentsUser = $xpath->query('//div[@class="row"]/div[@class="stamp"]/p[@class="stamp"]/span[@class="user category0"]');foreach($commentsUser as $inner) {$commentUser[]=$inner->nodeValue;}
    $commentsCity = $xpath->query('//div[@class="row"]/div[@class="stamp"]/p[@class="stamp"]/span[@class="city"]');foreach($commentsCity as $inner) {$commentCity[]=$inner->nodeValue;}
    $commentsDate = $xpath->query('//div[@class="row"]/div[@class="stamp"]/p[@class="stamp"]/span[@class="date"]');foreach($commentsDate as $inner) {$commentDate[]=$inner->nodeValue;}

    $print['Reviews'] = array();
    $print['Comments'] = array();
    
    $g=0;
    for($i=0;$i<count($review);$i++){
        if($grade[$g] == "Отлично!")
            $print['Reviews']['r'.$g]['Grade'] = "5";
        elseif($grade[$g] == "Хорошо")
            $print['Reviews']['r'.$g]['Grade'] = "4";
        elseif($grade[$g] == "Нормально")
            $print['Reviews']['r'.$g]['Grade'] = "3";
        elseif($grade[$g] == "Плохо")
            $print['Reviews']['r'.$g]['Grade'] = "2";
        elseif($grade[$g] == "Ужасно!")
            $print['Reviews']['r'.$g]['Grade'] = "1";
        else
            $print['Reviews']['r'.$g]['Grade'] = "0";
            
        $print['Reviews']['r'.$g]['Plus']     = $review[$i];
        $print['Reviews']['r'.$g]['Minus']    = $review[$i+1];
        $print['Reviews']['r'.$g]['Comment']  = $review[$i+2];
        
        $print['Reviews']['r'.$g]['User'] = $reviewUser[$g];
        $print['Reviews']['r'.$g]['City'] = $reviewCity[$g];
        $print['Reviews']['r'.$g]['Date'] = $reviewDate[$g];
        
        $i=$i+2;
        $g++;
    }

    for($i=0;$i<count($comment);$i++){
        $print['Comments']['c'.$i]['Num'] = $i;
        $print['Comments']['c'.$i]['Comment'] = $comment[$i];
        $print['Comments']['c'.$i]['User'] = $commentUser[$i];
        $print['Comments']['c'.$i]['City'] = $commentCity[$i];
        $print['Comments']['c'.$i]['Date'] = $commentDate[$i];
    }

    curl_close($curl);
    
    return json_encode($print);
}

function getShops($city){
    $result = array();
    $curl = curl_init();
    
    $name     = array();
    $address  = array();
    
    $html = getShopsHtml($curl, $city);
    $dom = new domDocument;
    @$dom->loadHTML($html); 
    $xpath = new DOMXPath($dom);
    $names = $xpath->query('//h2[@class="nomargin"]/a');foreach($names as $inner) {$name[]=$inner->nodeValue;}
    $addresses = $xpath->query('//div[@class="large"]');foreach($addresses as $inner) {$address[]=$inner->nodeValue;}
    
    $i=0;
    foreach($names as $inner) {
        $result["t$i"] = array('Name'=>$name[$i], 'Address'=>$address[$i]);
        $i++;
    }
    curl_close($curl);
    
    return json_encode($result);
}

function getSearch($city, $search){
    $result = array();
    $curl = curl_init();
    
    $code     = array();
    $name     = array();
    $price    = array();
    
    $html = getSearchHtml($curl, $city, $search);
    
    $dom = new domDocument;
    @$dom->loadHTML($html); 
    $xpath = new DOMXPath($dom);
    
    $itemsCode = $xpath->query('//tr[@itemscope]/td[@class="c"]');foreach($itemsCode as $inner) {$code[]     =$inner->nodeValue;}
    
    if($xpath->query('//h1[@itemprop="name"]')->item(0)->nodeValue && $xpath->query('//h1[@itemprop="name"]')->item(0)->nodeValue !== ""){
        $result['Type'] = "item";
        $result['Code'] = $xpath->query('//li[@class="code"]/span')->item(0)->nodeValue;
    }
    elseif(count($code)>0){
        $result['Type'] = "items";
        $items = $xpath->query('//tr[@itemscope]');
        $itemsCode = $xpath->query('//tr[@itemscope]/td[@class="c"]');
        foreach($itemsCode as $inner)
            $code[]     =$inner->nodeValue;
        $itemsName = $xpath->query('//tr[@itemscope]/td[@class="t"]/a/span[@itemprop="name"]');foreach($itemsName as $inner) {$name[]=$inner->nodeValue;}
        $itemsPrice = $xpath->query('//tr[@itemscope]/td[@class="p"]/div');foreach($itemsPrice as $inner) {$price[]=$inner->nodeValue;}
        $i=0;
        foreach($items as $inner) {
            $pr = $price[$i];
            $result['Items']["t$i"] = array('Code'=>$code[$i], 'Name'=>$name[$i], 'Price'=>$pr);
            $i++;
        }
    }else{
        $message = $xpath->query('//div[@class="message"]/p');
        $result['Type'] = "no";
        $result['Message'] = $message->item(0)->nodeValue;
    }
    curl_close($curl);
    
    return json_encode($result);
}

function getItemsHtml($curl, $city, $id, $errCount = 1){
    $url = "http://".$city.".dns-shop.ru/catalog/".$id."/?length_3=0";
    
    curl_setopt($curl, CURLOPT_URL, $url);
    curl_setopt($curl, CURLOPT_HEADER, 0);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($curl, CURLOPT_USERAGENT, 'Opera 10.00');
    $html = curl_exec($curl);
    
    if (curl_getinfo($curl,CURLINFO_HTTP_CODE) != 200) {
        return (($errCount < 2) ? getItemsHtml($curl, $city, $id, ++$errCount) : false);
    } else {
        return $html;
    }
}

function getItemsHtmlByVendor($curl, $city, $id, $vendor, $errCount = 1){
    $v = str_replace("f_", "f%5B",   $vendor);
    $v = str_replace("_",  "%5D%5B", $v);
    
    $url = "http://".$city.".dns-shop.ru/catalog/".$id."/?flex=on&length_3=0&fl=on&pf=&pt=&".$v."%5D=on";
    
    curl_setopt($curl, CURLOPT_URL, $url);
    curl_setopt($curl, CURLOPT_HEADER, 0);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($curl, CURLOPT_USERAGENT, 'Opera 10.00');
    $html = curl_exec($curl);
    
    if (curl_getinfo($curl,CURLINFO_HTTP_CODE) != 200) {
        return (($errCount < 2) ? getItemsHtmlByVendor($curl, $city, $id, $vendor, ++$errCount) : false);
    } else {
        return $html;
    }
}

function getItemHtml($curl, $city, $id, $errCount = 1){
    $url = "http://".$city.".dns-shop.ru/catalog/i".$id."/";
    curl_setopt($curl, CURLOPT_URL, $url);
    curl_setopt($curl, CURLOPT_FOLLOWLOCATION, true);
    curl_setopt($curl, CURLOPT_HEADER, 0);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($curl, CURLOPT_USERAGENT, 'Opera 10.00');
    $html = curl_exec($curl);
    
    if (curl_getinfo($curl,CURLINFO_HTTP_CODE) != 200) {
        return (($errCount < 2) ? getItemHtml($curl, $city, $id, ++$errCount) : false);
    } else {
        return $html;
    }
}

function getShopsHtml($curl, $city, $errCount = 1){
    $url = "http://pda.dns-shop.ru/".$city."/shop";
    curl_setopt($curl, CURLOPT_URL, $url);
    curl_setopt($curl, CURLOPT_FOLLOWLOCATION, true);
    curl_setopt($curl, CURLOPT_HEADER, 0);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($curl, CURLOPT_USERAGENT, 'Opera 10.00');
    $html = curl_exec($curl);
    
    if (curl_getinfo($curl,CURLINFO_HTTP_CODE) != 200) {
        return (($errCount < 2) ? getShopsHtml($curl, $city, ++$errCount) : false);
    } else {
        return $html;
    }
}

function getSearchHtml($curl, $city, $search, $errCount = 1){
    $url = "http://".$city.".dns-shop.ru/search/?q=".$search;
    curl_setopt($curl, CURLOPT_URL, $url);
    curl_setopt($curl, CURLOPT_FOLLOWLOCATION, true);
    curl_setopt($curl, CURLOPT_HEADER, 0);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($curl, CURLOPT_USERAGENT, 'Opera 10.00');
    $html = curl_exec($curl);
    
    if (curl_getinfo($curl,CURLINFO_HTTP_CODE) != 200) {
        return (($errCount < 2) ? getSearchHtml($curl, $city, $search, ++$errCount) : false);
    } else {
        return $html;
    }
}

gzdocout();

exit();