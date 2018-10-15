<?php

// autoload_static.php @generated by Composer

namespace Composer\Autoload;

class ComposerStaticInitUpdateNotification
{
    public static $prefixLengthsPsr4 = array (
        'O' => 
        array (
            'OCA\\UpdateNotification\\' => 23,
        ),
    );

    public static $prefixDirsPsr4 = array (
        'OCA\\UpdateNotification\\' => 
        array (
            0 => __DIR__ . '/..' . '/../lib',
        ),
    );

    public static $classMap = array (
        'OCA\\UpdateNotification\\AppInfo\\Application' => __DIR__ . '/..' . '/../lib/AppInfo/Application.php',
        'OCA\\UpdateNotification\\Controller\\APIController' => __DIR__ . '/..' . '/../lib/Controller/APIController.php',
        'OCA\\UpdateNotification\\Controller\\AdminController' => __DIR__ . '/..' . '/../lib/Controller/AdminController.php',
        'OCA\\UpdateNotification\\Notification\\BackgroundJob' => __DIR__ . '/..' . '/../lib/Notification/BackgroundJob.php',
        'OCA\\UpdateNotification\\Notification\\Notifier' => __DIR__ . '/..' . '/../lib/Notification/Notifier.php',
        'OCA\\UpdateNotification\\ResetTokenBackgroundJob' => __DIR__ . '/..' . '/../lib/ResetTokenBackgroundJob.php',
        'OCA\\UpdateNotification\\Settings\\Admin' => __DIR__ . '/..' . '/../lib/Settings/Admin.php',
        'OCA\\UpdateNotification\\UpdateChecker' => __DIR__ . '/..' . '/../lib/UpdateChecker.php',
    );

    public static function getInitializer(ClassLoader $loader)
    {
        return \Closure::bind(function () use ($loader) {
            $loader->prefixLengthsPsr4 = ComposerStaticInitUpdateNotification::$prefixLengthsPsr4;
            $loader->prefixDirsPsr4 = ComposerStaticInitUpdateNotification::$prefixDirsPsr4;
            $loader->classMap = ComposerStaticInitUpdateNotification::$classMap;

        }, null, ClassLoader::class);
    }
}
