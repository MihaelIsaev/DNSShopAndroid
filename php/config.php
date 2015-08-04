<?php

ini_set('session.gc_maxlifetime', 31536000);
session_start();

if (defined('DEBUG') && DEBUG)
{
    error_reporting(E_ALL ^ E_NOTICE);
    ini_set('display_errors', 1);
}

define('SITE_PATH', dirname(__FILE__));
define('SITE_URL', 'http://___');
define('DS', DIRECTORY_SEPARATOR);

define('AJAX_PATH', SITE_PATH.DS.'../ajax/');
define('CONTENT_PATH', SITE_PATH.DS.'content/');
define('MDL_PATH', SITE_PATH.DS.'modules/');
define('TMPL_PATH', SITE_PATH.DS.'templates/');

define('CLASS_PATH', SITE_PATH.DS.'../classes/');

define('IMAGES_PATH', CONTENT_PATH.DS.'content/images/');


function __autoload($class_name)
{
    $class_file = CLASS_PATH.DS.$class_name.'.php';
    if (file_exists($class_file)) {
        require_once($class_file);
    }
}

header('Content-Type: text/html; charset=UTF-8');