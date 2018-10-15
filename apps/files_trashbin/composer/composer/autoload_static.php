<?php

// autoload_static.php @generated by Composer

namespace Composer\Autoload;

class ComposerStaticInitFiles_Trashbin
{
    public static $prefixLengthsPsr4 = array (
        'O' => 
        array (
            'OCA\\Files_Trashbin\\' => 19,
        ),
    );

    public static $prefixDirsPsr4 = array (
        'OCA\\Files_Trashbin\\' => 
        array (
            0 => __DIR__ . '/..' . '/../lib',
        ),
    );

    public static $classMap = array (
        'OCA\\Files_Trashbin\\AppInfo\\Application' => __DIR__ . '/..' . '/../lib/AppInfo/Application.php',
        'OCA\\Files_Trashbin\\BackgroundJob\\ExpireTrash' => __DIR__ . '/..' . '/../lib/BackgroundJob/ExpireTrash.php',
        'OCA\\Files_Trashbin\\Capabilities' => __DIR__ . '/..' . '/../lib/Capabilities.php',
        'OCA\\Files_Trashbin\\Command\\CleanUp' => __DIR__ . '/..' . '/../lib/Command/CleanUp.php',
        'OCA\\Files_Trashbin\\Command\\Expire' => __DIR__ . '/..' . '/../lib/Command/Expire.php',
        'OCA\\Files_Trashbin\\Command\\ExpireTrash' => __DIR__ . '/..' . '/../lib/Command/ExpireTrash.php',
        'OCA\\Files_Trashbin\\Controller\\PreviewController' => __DIR__ . '/..' . '/../lib/Controller/PreviewController.php',
        'OCA\\Files_Trashbin\\Events\\MoveToTrashEvent' => __DIR__ . '/..' . '/../lib/Events/MoveToTrashEvent.php',
        'OCA\\Files_Trashbin\\Exceptions\\CopyRecursiveException' => __DIR__ . '/..' . '/../lib/Exceptions/CopyRecursiveException.php',
        'OCA\\Files_Trashbin\\Expiration' => __DIR__ . '/..' . '/../lib/Expiration.php',
        'OCA\\Files_Trashbin\\Helper' => __DIR__ . '/..' . '/../lib/Helper.php',
        'OCA\\Files_Trashbin\\Hooks' => __DIR__ . '/..' . '/../lib/Hooks.php',
        'OCA\\Files_Trashbin\\Sabre\\ITrash' => __DIR__ . '/..' . '/../lib/Sabre/ITrash.php',
        'OCA\\Files_Trashbin\\Sabre\\PropfindPlugin' => __DIR__ . '/..' . '/../lib/Sabre/PropfindPlugin.php',
        'OCA\\Files_Trashbin\\Sabre\\RestoreFolder' => __DIR__ . '/..' . '/../lib/Sabre/RestoreFolder.php',
        'OCA\\Files_Trashbin\\Sabre\\RootCollection' => __DIR__ . '/..' . '/../lib/Sabre/RootCollection.php',
        'OCA\\Files_Trashbin\\Sabre\\TrashFile' => __DIR__ . '/..' . '/../lib/Sabre/TrashFile.php',
        'OCA\\Files_Trashbin\\Sabre\\TrashFolder' => __DIR__ . '/..' . '/../lib/Sabre/TrashFolder.php',
        'OCA\\Files_Trashbin\\Sabre\\TrashFolderFile' => __DIR__ . '/..' . '/../lib/Sabre/TrashFolderFile.php',
        'OCA\\Files_Trashbin\\Sabre\\TrashFolderFolder' => __DIR__ . '/..' . '/../lib/Sabre/TrashFolderFolder.php',
        'OCA\\Files_Trashbin\\Sabre\\TrashHome' => __DIR__ . '/..' . '/../lib/Sabre/TrashHome.php',
        'OCA\\Files_Trashbin\\Sabre\\TrashRoot' => __DIR__ . '/..' . '/../lib/Sabre/TrashRoot.php',
        'OCA\\Files_Trashbin\\Storage' => __DIR__ . '/..' . '/../lib/Storage.php',
        'OCA\\Files_Trashbin\\Trashbin' => __DIR__ . '/..' . '/../lib/Trashbin.php',
    );

    public static function getInitializer(ClassLoader $loader)
    {
        return \Closure::bind(function () use ($loader) {
            $loader->prefixLengthsPsr4 = ComposerStaticInitFiles_Trashbin::$prefixLengthsPsr4;
            $loader->prefixDirsPsr4 = ComposerStaticInitFiles_Trashbin::$prefixDirsPsr4;
            $loader->classMap = ComposerStaticInitFiles_Trashbin::$classMap;

        }, null, ClassLoader::class);
    }
}
