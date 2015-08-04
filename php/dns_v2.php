<?php
require_once 'config.php';
include('gzdoc.php'); 

$what           = $_POST['what'];
$city           = $_POST['city'];
$id             = $_POST['id'];
$filters        = $_POST['filters'];
$search         = $_POST['search'];

switch($what){
    case "items":
        if($id=="185"){$id="106";}
        echo getItems($city, $id, $filters);
        break;
    case "item":
        echo getItem($city, $id);
        break;
    case "itemMoreReviews":
        echo getItemMoreReviews($city, $id);
        break;
    case "shops":
        echo getShops($city);
        break;
    case "search":
        echo getSearch($city, $search);
        break;
    case "cityes":
        echo getCityes();
        break;
}

function getCityes(){
    $db = new mysqliDB();
    $city = $db->select("SELECT * FROM `dns`.`city`");

    $cityes = array();

    for($i=0;$i<count($city);$i++){
        $cityes['t'.$i]['Id']           = $city[$i]['Id'];
        $cityes['t'.$i]['Name']         = $city[$i]['Name'];        
        $cityes['t'.$i]['Phone']        = $city[$i]['Phone'];
        $cityes['t'.$i]['Longitude']    = $city[$i]['Longitude'];
        $cityes['t'.$i]['Latitude']     = $city[$i]['Latitude'];
    }

    echo json_encode($cityes);
}

function getFilters($xpath){
    $result = array();
    
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
                $result["n".$i]["Values"]["n".$n]["Key"] = $innerI->getAttribute("id");
                $result["n".$i]["Values"]["n".$n]["Name"] = $label->item($n+1)->nodeValue;                
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
                if($n==0){
                    $from = str_replace("][", "_", $innerI->getAttribute("name"));
                    $from = str_replace("[", "_", $from);
                    $from = str_replace("]", "", $from);
                    $result["n".$i]["From"] = $from;
                }elseif($n==1){
                    $to = str_replace("][", "_", $innerI->getAttribute("name"));
                    $to = str_replace("[", "_", $to);
                    $to = str_replace("]", "", $to);
                    $result["n".$i]["To"] = $to;
                }
                $n++;
            }
        }
        
        $i++;
    }
    
    return $result;
}

function getItems($city, $id, $filters){
    $result = array();
    $curl = curl_init();
    
    $code     = array();
    $comments = array();
    $previews = array();
    $name     = array();
    $price    = array();
    
    $filters = str_replace("f", "f%5B", $filters);
    $filters = str_replace("f%5B_", "f%5B", $filters);    
    $filters = str_replace("_", "%5D%5B", $filters);
    $filters = str_replace("=", "%5D=", $filters);
    $filters = str_replace("pf%5D", "pf", $filters);
    $filters = str_replace("pt%5D", "pt", $filters);
    $filters = str_replace("pf%5B%5D=", "pf=", $filters);
    
    $html = getItemsHtml($curl, $city, $id, $filters);
    $dom = new domDocument;
    @$dom->loadHTML($html); 
    $xpath = new DOMXPath($dom);
    $items = $xpath->query('//td[@class="t"]');
    $itemsCode = $xpath->query('//td[@class="c"]');
    foreach($itemsCode as $inner) {
        $code[]     =$inner->nodeValue;
    }
    $itemsComments = $xpath->query('//td[@class="co"]/a');
    foreach($itemsComments as $inner) {
        $comments[]     = $inner->nodeValue;
    }
    $itemsPreviews = $xpath->query('//td[@class="t"]/a/img');
    foreach($itemsPreviews as $inner) {
        $previews[]     = $inner->getAttribute("src");
    }
    
    $itemsName = $xpath->query('//td[@class="t"]');foreach($itemsName as $inner) {$name[]=$inner->nodeValue;}
    $itemsGrade = $xpath->query('//td[@class="r"]');
    $nnn=0;
    foreach($itemsGrade as $inner) {
        $doc2 = new DOMDocument();
        $doc2->appendChild($doc2->importNode($itemsGrade->item($nnn), true));
        $priceSheetHtml = $doc2->saveHTML();
        @$domTmp = new domDocument;
        @$domTmp->loadHTML($priceSheetHtml); 
        $xpathTmp = new DOMXPath($domTmp);
        
        
        
        $ttt = $xpathTmp->query("//a");
        $ttti=0;
        foreach($ttt as $inner) {$ttti++;}
        if($ttti>0){
           $tmpGr = $ttt->item(0)->getAttribute("title");
           $tmpGr = split(" от ", $tmpGr);
           $tmp2 = split(",", $tmpGr[0]);
           $grade[] = $tmp2[0];
        }else{
            $grade[] = 0;
        }
        
        $nnn++;
    }
    $itemsPrice = $xpath->query('//td[@class="p"]/div');
    foreach($itemsPrice as $inner) {if($inner->getAttribute("class")!=="prev")$price[]=$inner->nodeValue;}
    $i=0;
    $result["items_count"] = count($items);
    foreach($items as $inner) {
        $pr = str_replace(" ", "", $price[$i]);
        if($id=="106"){$id="185";}
        $result['Items']["t$i"] = array('Parent'=> $id, 'Code'=>$code[$i], 'Comments'=>$comments[$i], 'Name'=>str_replace("Помощь эксперта ", "", $name[$i]), 'Price'=>$pr, 'Grade'=>$grade[$i], "Preview"=>$previews[$i]);
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
    $availFuture = array();
    $grade = array();
    $review = array();
    $comment = array();
    $imgC = array();
    $reviewUser = array();
    $reviewCity = array();
    $reviewDate = array();
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
    $img = $xpath->query('//img[@id="main_ill_img"]');foreach($img as $inner) {$imgC[]=$inner->nodeValue;}
    //Цена со скидкой, какбы новая
    $priceNew = $xpath->query('//li[@class="price"]/div[@class="new"]/span[@class="value"]');
    //Описание
    $descr = $xpath->query('//div[@itemprop="description"]');
    //Спец
    $spec = $xpath->query('//div[@id="catalog_item_options"]');
    //Комментарии
    $comm = $xpath->query('//div[@id="comment_list"]');

    
    if($price->item(0)->nodeValue == "")
        $pr = $priceNew->item(0)->nodeValue;
    else
        $pr = $price->item(0)->nodeValue;

    $doc2 = new DOMDocument();
    $doc2->appendChild($doc2->importNode($spec->item(0), true));
    $priceSheetHtml = $doc2->saveHTML();
    
    $comments = $xpath->query('//div[@class="box"]/p');
    foreach($comments as $inner) {
        $docT = new DOMDocument();
        $docT->appendChild($docT->importNode($inner, true));
        $htmlT = $docT->saveHTML();
        $comment[]= $htmlT;
    }
    if(count($comment)>0){  
        $doc3 = new DOMDocument();
        $doc3->appendChild($doc3->importNode($comm->item(0), true));
        $commSheetHtml = $doc3->saveHTML();
    }

    $print['Name']        = $name->item(0)->nodeValue;
    $print['Code']        = "Код: ".$code->item(0)->nodeValue;
    if(count($imgC)>0)
        $print['Image']       = $img->item(0)->getAttribute(src);
    else
       $print['Image'] = ""; 
    $print['Price']       = $pr;
    $print['Description'] = $descr->item(0)->nodeValue;
    $print['Features']    = $priceSheetHtml;
    $print['Comments']    = $commSheetHtml;

    //Наличие
    $avails = $xpath->query('//div[@class="avail"]/ul/li/a');foreach($avails as $inner) {$avail[]=$inner->nodeValue;}
    //Наличие СКОРО
    $availsFuture = $xpath->query('//div[@class="transit"]/ul/li/a');foreach($availsFuture as $inner) {$availFuture[]=$inner->nodeValue;}
    
    $av = "";
    for($i=0;$i<count($avail);$i++)
        $av .= " ".$avail[$i];
    $avF = "";
    if(count($availFuture)>0){
        $avF .= " скоро в ";
        for($i=0;$i<count($availFuture);$i++)
            $avF .= " ".$availFuture[$i];
    }
    $print['Availability'] = "Наличие: ".$av.$avF;
    
    //Отзывы
    $reviewsH = $xpath->query('//div[@id="opinion_list"]/ul/li');///ul/li/div[@class="text"]');
    $kl=0;
    foreach($reviewsH as $inner) {
        $docR = new DOMDocument();
        $docR->appendChild($docR->importNode($reviewsH->item($kl), true));
        $reviewsHTML = $docR->saveHTML();
        $domR = new domDocument;
        @$domR->loadHTML($reviewsHTML); 
        $xpathR = new DOMXPath(@$domR);
        $reviews = $xpathR->query('//div[@class="text"]');
        
        $kll=0;
        foreach($reviews as $inner) {
            $docT = new DOMDocument();
            $docT->appendChild($docT->importNode($inner, true));
            $htmlT = $docT->saveHTML();
            $htmlT = str_replace("<div class=\"text\" itemprop=\"reviewBody\">", "", $htmlT);
            $htmlT = str_replace("</div>", "", $htmlT);
            if($kll==0)
                $ttt = "Plus";
            else if($kll==1)
                $ttt = "Minus";
            else if($kll==2)
                $ttt = "Comment";
            $review[$kl][$ttt]= $htmlT;
            $kll++;
        }
        $kl++;
    }

    //Оценки
    $grades = $xpath->query('//div[@id="opinion_list"]/ul/li/div/div[@class="grade"]');foreach($grades as $inner) {$grade[]=$inner->nodeValue;}
    
    $reviewsUser = $xpath->query('//div[@id="opinion_list"]/ul/li/div[@class="last_block"]/p[@class="stamp"]/span[@class="user category0"]');foreach($reviewsUser as $inner) {$reviewUser[]=$inner->nodeValue;}
    $reviewsCity = $xpath->query('//div[@id="opinion_list"]/ul/li/div[@class="last_block"]/p[@class="stamp"]/span[@class="city"]');foreach($reviewsCity as $inner) {$reviewCity[]=$inner->nodeValue;}
    $reviewsDate = $xpath->query('//div[@id="opinion_list"]/ul/li/div[@class="last_block"]/p[@class="stamp"]/span[@class="date"]');foreach($reviewsDate as $inner) {$reviewDate[]=$inner->nodeValue;}

    $print['Reviews'] = array();
    
    for($i=0;$i<$kl;$i++){
        if($grade[$i] == "Отлично!")
            $print['Reviews']['r'.$i]['Grade'] = "5";
        elseif($grade[$i] == "Хорошо")
            $print['Reviews']['r'.$i]['Grade'] = "4";
        elseif($grade[$i] == "Нормально")
            $print['Reviews']['r'.$i]['Grade'] = "3";
        elseif($grade[$i] == "Плохо")
            $print['Reviews']['r'.$i]['Grade'] = "2";
        elseif($grade[$i] == "Ужасно!")
            $print['Reviews']['r'.$i]['Grade'] = "1";
        else
            $print['Reviews']['r'.$i]['Grade'] = "0";
            
        $print['Reviews']['r'.$i]['Plus']     = $review[$i]["Plus"];
        $print['Reviews']['r'.$i]['Minus']    = $review[$i]["Minus"];
        $print['Reviews']['r'.$i]['Comment']  = $review[$i]["Comment"];
        
        $print['Reviews']['r'.$i]['User'] = $reviewUser[$i];
        $print['Reviews']['r'.$i]['City'] = $reviewCity[$i];
        $print['Reviews']['r'.$i]['Date'] = $reviewDate[$i];
    }

    curl_close($curl);
    
    return json_encode($print);
}

function getItemMoreReviews($city, $id){
    $print = array();

    $curl = curl_init();
    
    $res = getItemMoreReviewsHtml($curl, $city, $id);
    $j = explode("\"text\":\"", $res);
    $k = explode("\",\"by_id\"", $j[1]);
    $html = "<html><head></head><body><ul>".$k[0]."</ul></body></html>";
    $html = str_replace("\\\"", "\"", $html);
    $html = str_replace("\\/", "/", $html);
    $dom = new domDocument;
    @$dom->loadHTML($html); 
    $xpath = new DOMXPath($dom);
    $grade = array();
    $review = array();
    $reviewUser = array();
    $reviewCity = array();
    $reviewDate = array();
    
    //Отзывы
    $reviewsH = $xpath->query('//li[@itemprop="reviews"]');///ul/li/div[@class="text"]');
    $kl=0;
    foreach($reviewsH as $inner) {
        $docR = new DOMDocument();
        $docR->appendChild($docR->importNode($reviewsH->item($kl), true));
        $reviewsHTML = $docR->saveHTML();
        $domR = new domDocument;
        @$domR->loadHTML($reviewsHTML); 
        $xpathR = new DOMXPath(@$domR);
        $reviews = $xpathR->query('//div[@class="text"]');
        
        $kll=0;
        foreach($reviews as $inner) {
            $docT = new DOMDocument();
            $docT->appendChild($docT->importNode($inner, true));
            $htmlT = $docT->saveHTML();
            $htmlT = str_replace("<div class=\"text\" itemprop=\"reviewBody\">", "", $htmlT);
            $htmlT = str_replace("</div>", "", $htmlT);
            if($kll==0)
                $ttt = "Plus";
            else if($kll==1)
                $ttt = "Minus";
            else if($kll==2)
                $ttt = "Comment";
            $review[$kl][$ttt]= $htmlT;
            $kll++;
        }
        $kl++;
    }

    //Оценки
    $grades = $xpath->query('//div/div[@class="grade"]');foreach($grades as $inner) {$grade[]=$inner->nodeValue;}
    
    $reviewsUser = $xpath->query('//div[@class="last_block"]/p[@class="stamp"]/span[@class="user category0"]');foreach($reviewsUser as $inner) {$reviewUser[]=$inner->nodeValue;}
    $reviewsCity = $xpath->query('//div[@class="last_block"]/p[@class="stamp"]/span[@class="city"]');foreach($reviewsCity as $inner) {$reviewCity[]=$inner->nodeValue;}
    $reviewsDate = $xpath->query('//div[@class="last_block"]/p[@class="stamp"]/span[@class="date"]');foreach($reviewsDate as $inner) {$reviewDate[]=$inner->nodeValue;}

    for($i=0;$i<$kl;$i++){
        $t = json_decode("{\"t\":\"".$grade[$i]."\"}");
        if($t->t == "Отлично!")
            $print['r'.$i]['Grade'] = "5";
        elseif($t->t == "Хорошо")
            $print['r'.$i]['Grade'] = "4";
        elseif($t->t == "Нормально")
            $print['r'.$i]['Grade'] = "3";
        elseif($t->t == "Плохо")
            $print['r'.$i]['Grade'] = "2";
        elseif($t->t == "Ужасно!")
            $print['r'.$i]['Grade'] = "1";
        else
            $print['r'.$i]['Grade'] = "0";
            
        $print['r'.$i]['Plus']     = unenc_utf16_code_units($review[$i]["Plus"]);
        $print['r'.$i]['Minus']    = unenc_utf16_code_units($review[$i]["Minus"]);
        $print['r'.$i]['Comment']  = unenc_utf16_code_units($review[$i]["Comment"]);
        
        
        
        $print['r'.$i]['User'] = unenc_utf16_code_units($reviewUser[$i]);
        $print['r'.$i]['City'] = unenc_utf16_code_units($reviewCity[$i]);
        $print['r'.$i]['Date'] = unenc_utf16_code_units($reviewDate[$i]);
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
        $result["t$i"] = array('Name'=>$name[$i], 'Address'=>$address[$i], 'WorkTime'=>"");
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
    
    $newSearch = str_replace(" ",  "+", $search);
    
    $html = getSearchHtml($curl, $city, $newSearch);
    
    $dom = new domDocument;
    @$dom->loadHTML($html); 
    $xpath = new DOMXPath($dom);
    
    $itemsCode = $xpath->query('//td[@class="c"]');
    foreach($itemsCode as $inner) {
        $code[] = $inner->nodeValue;
    }
    
    if($xpath->query('//h1[@itemprop="name"]')->item(0)->nodeValue && $xpath->query('//h1[@itemprop="name"]')->item(0)->nodeValue !== ""){
        $result['Type'] = "item";
        $result['Code'] = $xpath->query('//li[@class="code"]/span')->item(0)->nodeValue;
    }
    elseif(count($code)>0){
        $result['Type'] = "items";
        $items = $xpath->query('//td[@class="t"]');
        $itemsComments = $xpath->query('//td[@class="co"]/a');
        foreach($itemsComments as $inner) {
            $comments[] = $inner->nodeValue;
        }
        $itemsImg = $xpath->query('//tr[td[@class="p"]]');foreach($itemsImg as $inner) {$imgs[]=$inner->getAttribute("preview");}
        $itemsName = $xpath->query('//td[@class="t"]');foreach($itemsName as $inner) {$name[]=$inner->nodeValue;}
        $itemsGrade = $xpath->query('//td[@class="r"]');
        $nnn=0;
        foreach($itemsGrade as $inner) {
            $doc2 = new DOMDocument();
            $doc2->appendChild($doc2->importNode($itemsGrade->item($nnn), true));
            $priceSheetHtml = $doc2->saveHTML();
            @$domTmp = new domDocument;
            @$domTmp->loadHTML($priceSheetHtml); 
            $xpathTmp = new DOMXPath($domTmp);



            $ttt = $xpathTmp->query("//a");
            $ttti=0;
            foreach($ttt as $inner) {$ttti++;}
            if($ttti>0){
            $tmpGr = $ttt->item(0)->getAttribute("title");
            $tmpGr = split(" от ", $tmpGr);
            $tmp2 = split(",", $tmpGr[0]);
            $grade[] = $tmp2[0];
            }else{
                $grade[] = 0;
            }

            $nnn++;
        }
        $itemsPrice = $xpath->query('//td[@class="p"]/div');
        foreach($itemsPrice as $inner) {if($inner->getAttribute("class")!=="prev")$price[]=$inner->nodeValue;}
        $i=0;
        foreach($items as $inner) {
            $pr = str_replace(" ", "", $price[$i]);
            $result['Items']["t".$i] = array('Code'=>$code[$i], 'Name'=>str_replace("Помощь эксперта ", "", $name[$i]), 'Price'=>$pr);
            $i++;
        }
        
        
        
        /*
        $items = $xpath->query('//tr[@itemscope]');
        $itemsCode = $xpath->query('//tr[@itemscope]/td[@class="c"]');
        foreach($itemsCode as $inner)
            $code[]     =$inner->nodeValue;
        $itemsName = $xpath->query('//tr[@itemscope]/td[@class="t"]/a/span[@itemprop="name"]');foreach($itemsName as $inner) {$name[]=$inner->nodeValue;}
        $itemsPrice = $xpath->query('//tr[@itemscope]/td[@class="p"]/div');
        foreach($itemsPrice as $inner) {if($inner->getAttribute("class")!=="prev")$price[]=$inner->nodeValue;}
        $i=0;
        foreach($items as $inner) {
            $pr = str_replace(" ", "", $price[$i]);
            $result['Items']["t$i"] = array('Code'=>$code[$i], 'Name'=>$name[$i], 'Price'=>$pr);
            $i++;
        }*/
    }else{
        $message = $xpath->query('//div[@class="message"]/p');
        $result['Type'] = "no";
        $result['Message'] = $message->item(0)->nodeValue;
    }
    curl_close($curl);
    
    return json_encode($result);
}

function getItemsHtml($curl, $city, $id, $filters, $errCount = 1){
    $url = "http://".$city.".dns-shop.ru/catalog/".$id."/?flex=on&fl=on&length_1=0".$filters;
    
    @$h = fopen("log_url.txt","w");
    @fwrite($h,$url);
    @fclose($h);
    
    curl_setopt($curl, CURLOPT_URL, $url);
    curl_setopt($curl, CURLOPT_HEADER, 0);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($curl, CURLOPT_USERAGENT, 'Opera 10.00');
    $html = curl_exec($curl);
    
    if (curl_getinfo($curl,CURLINFO_HTTP_CODE) != 200)
        return (($errCount < 2) ? getItemsHtml($curl, $city, $id, $filters, ++$errCount) : false);
    else
        return $html;
}

function getItemsHtmlByVendor($curl, $city, $id, $vendor, $errCount = 1){
    $v = str_replace("f_", "f%5B",   $vendor);
    $v = str_replace("_",  "%5D%5B", $v);
    
    $url = "http://".$city.".dns-shop.ru/catalog/".$id."/?length_3=0&fl=on&pf=&pt=&".$v."%5D=on";
    
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

function getItemMoreReviewsHtml($curl, $city, $id, $errCount = 1){
    $url = "http://".$city.".dns-shop.ru/engine/scripts/ajax.php?action=opinion_load&module_id=1&object_id=".$id."&page=1&id=0&all=1";
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
    $url = "http://".$city.".dns-shop.ru/search/?length_3=0&q=".$search;
    curl_setopt($curl, CURLOPT_URL, $url);
    curl_setopt($curl, CURLOPT_FOLLOWLOCATION, true);
    curl_setopt($curl, CURLOPT_HEADER, 0);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($curl, CURLOPT_USERAGENT, 'Opera 12.50');
    $html = curl_exec($curl);
    
    if (curl_getinfo($curl,CURLINFO_HTTP_CODE) != 200) {
        return (($errCount < 2) ? getSearchHtml($curl, $city, $search, ++$errCount) : false);
    } else {
        return $html;
    }
}

function unenc_utf16_code_units($string) {
    /* go for possible surrogate pairs first */
    $string = preg_replace_callback(
        '/\\\\U(D[89ab][0-9a-f]{2})\\\\U(D[c-f][0-9a-f]{2})/i',
        function ($matches) {
            $hi_surr = hexdec($matches[1]);
            $lo_surr = hexdec($matches[2]);
            $scalar = (0x10000 + (($hi_surr & 0x3FF) << 10) |
                ($lo_surr & 0x3FF));
            return "&#x" . dechex($scalar) . ";";
        }, $string);
    /* now the rest */
    $string = preg_replace_callback('/\\\\U([0-9a-f]{4})/i',
        function ($matches) {
            //just to remove leading zeros
            return "&#x" . dechex(hexdec($matches[1])) . ";";
        }, $string);
    return $string;
}

gzdocout();

exit();