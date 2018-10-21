<?php
/**
 * Load Javascrip
 */

use OCP\Util;

$eventDispatcher = \OC::$server->getEventDispatcher();
$eventDispatcher->addListener('OCA\Files::loadAdditionalScripts', function(){
    Util::addScript('xpxhashes', 'xpxhashes.tabview' );
    Util::addScript('xpxhashes', 'xpxhashes.plugin' );
});

