# shell.nix
{ pkgs ? import <nixpkgs> {} }:

let
  javafxLibs = with pkgs; [
    gtk3
    glib
    libGL
    fontconfig
    freetype
    pango
    cairo
    gdk-pixbuf
    atk
    alsa-lib

    xorg.libX11
    xorg.libXext
    xorg.libXrender
    xorg.libXtst
    xorg.libXi
    xorg.libXxf86vm
    xorg.libXcursor
    xorg.libXrandr
    xorg.libXinerama
    xorg.libXcomposite
    xorg.libXdamage
    xorg.libXfixes
  ];
in
pkgs.mkShell {
  packages = with pkgs; [
    jdk21
    maven
  ] ++ javafxLibs;

  LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath javafxLibs;
}
