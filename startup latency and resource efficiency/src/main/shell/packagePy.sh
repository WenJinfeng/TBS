#!/bin/bash


function packageSingleFile(){
  cd $1
  zip "${2}.zip" "${2}.py"
}

function packageFileWithDependency(){
  cd "${1}"
  cd "package"
  zip -r9 "${OLDPWD}/${2}.zip"
  cd ${OLDPWD}
  zip -g ${2}.zip ${2}.py
}

packageSingleFile $1 $2