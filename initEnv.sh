#!/bin/bash
download_tassl()
{
  mkdir -p ~/.fisco/
  if [ "$(uname)" == "Darwin" ];then
    curl -LO https://github.com/FISCO-BCOS/LargeFiles/raw/master/tools/tassl_mac.tar.gz
    mv tassl_mac.tar.gz ~/.fisco/tassl.tar.gz
  else
    curl -LO https://github.com/FISCO-BCOS/LargeFiles/raw/master/tools/tassl.tar.gz
    mv tassl.tar.gz ~/.fisco/tassl.tar.gz
  fi
  tar -xvf ~/.fisco/tassl.tar.gz
}

build_node()
{
  local node_type="${1}"
  if [ "${node_type}" == "sm" ];then
      ./build_chain.sh -l 127.0.0.1:4 -g
      sed_cmd=$(get_sed_cmd)
      $sed_cmd 's/sm_crypto_channel=false/sm_crypto_channel=true/g' nodes/127.0.0.1/node*/config.ini
  else
      ./build_chain.sh -l 127.0.0.1:4
  fi
  ./nodes/127.0.0.1/fisco-bcos -v
  ./nodes/127.0.0.1/start_all.sh
}
download_build_chain()
{
  tag=$(curl -sS "https://gitee.com/api/v5/repos/FISCO-BCOS/FISCO-BCOS/tags" | grep -oe "\"name\":\"v[2-9]*\.[0-9]*\.[0-9]*\"" | cut -d \" -f 4 | sort -V | tail -n 1)
  LOG_INFO "--- current tag: $tag"
  curl -LO "https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/${tag}/build_chain.sh" && chmod u+x build_chain.sh
}
prepare_environment()
{
  ## prepare resources for amop demo
  bash gradlew build -x test
  cp -r nodes/127.0.0.1/sdk/* sdk-demo/dist/conf
}

download_tassl
./gradlew build -x test
#download_build_chain
#build_node
#prepare_environment
