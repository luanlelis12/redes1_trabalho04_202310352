/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 26 09 2025
* Ultima alteracao.: 23 11 2025
* Nome.............: CamadaEnlaceDadosReceptora
* Funcao...........: simula a camada de enlace de dados receptora
*************************************************************** */

package model;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import util.ConversorStringBinario;
import util.TabelaCRC;

public class CamadaEnlaceDadosReceptora {
 
  private int tipoDeEnquadramento = 0;
  private int tipoDeControleDeErro = 0;
  private int tipoDeControleDeFluxo = 0;

  private final int ESPACO_SEQUENCIA = 8;
  private boolean erroNoQuadro = false;
  private int nsEsperado = 0;
  private final int TAMANHO_JANELA = 3;
  private int[][] bufferReceptor = new int[TAMANHO_JANELA][];

  private CamadaAplicacaoReceptora camadaAplicacaoReceptora;
  // referencia ao transmissor do mesmo host (para enviar ACKs)
  private CamadaEnlaceDadosTransmissora meuTransmissor;

  public void reset() {
    erroNoQuadro = false;
    nsEsperado = 0;
    for(int i=0; i < TAMANHO_JANELA; i++) {
        bufferReceptor[i] = null;
    }
  } // fim do metodo reset

  public CamadaEnlaceDadosReceptora (CamadaAplicacaoReceptora camadaAplicacaoReceptora) {
    this.camadaAplicacaoReceptora = camadaAplicacaoReceptora;
  }

  /* ***************************************************************
  * Metodo: setTipoDeEnquadramento
  * Funcao: Define o tipo de tipo de enquadramento que sera usada
  * Parametros: tipoDeEnquadramento = define um numero inteiro que representa um tipo de enquadramento
  * Retorno: void
  *************************************************************** */
  public void setTipoDeEnquadramento (int tipoDeEnquadramento) {
    this.tipoDeEnquadramento = tipoDeEnquadramento;
  } // fim do metodo setTipoDeEnquadramento

  /* ***************************************************************
  * Metodo: setTipoDeControleDeErro
  * Funcao: Define o tipo de tipo de controle de erro
  * Parametros: tipoDeControleDeErro = define um numero inteiro que representa um tipo de controle de erro
  * Retorno: void
  *************************************************************** */
  public void setTipoDeControleDeErro (int tipoDeControleDeErro) {
    this.tipoDeControleDeErro = tipoDeControleDeErro;
  } // fim do metodo setTipoDeControleDeErro

  /* ***************************************************************
  * Metodo: setTipoDeControleDeFluxo
  * Funcao: define o tipo de tipo de controle de fluxo
  * Parametros: tipoDeControleDeFluxo = define um numero inteiro que representa um tipo de controle de fluxo
  * Retorno: void
  *************************************************************** */
  public void setTipoDeControleDeFluxo(int tipoDeControleDeFluxo) {
    this.tipoDeControleDeFluxo = tipoDeControleDeFluxo;
  } // fim do metodo setTipoDeControleDeFluxo

  /* ***************************************************************
  * Metodo: setMeuTransmissor
  * Funcao: Define o tranmissor da camada de enlace
  * Parametros: meuTransmissor = o transmissor da camada de enlace de dados do host
  * Retorno: void
  *************************************************************** */
  public void setMeuTransmissor(CamadaEnlaceDadosTransmissora meuTransmissor) {
    this.meuTransmissor = meuTransmissor;
  } // fim do metodo setMeuTransmissor
  
  /* ***************************************************************
  * Metodo: camadaEnlaceDadosReceptora
  * Funcao: Executa todas as partes da camada de enlace
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: void
  *************************************************************** */
  public void camadaEnlaceDadosReceptora(int quadro[]) {
    
    quadro = camadaEnlaceDadosReceptoraControleDeErro(quadro);
    if (erroNoQuadro) {
      Platform.runLater(() -> {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Deteccao de Erro");
        alert.setHeaderText("QUADRO CORROMPIDO!");
        alert.setContentText("A Camada de Enlace Receptora detectou um erro no quadro recebido e o descartou.");
        alert.show();
      });
      erroNoQuadro=false;
      return;
    } // fim do if
    
    quadro = camadaEnlaceDadosReceptoraEnquadramento(quadro);
    
    quadro = camadaEnlaceDadosReceptoraControleDeFluxo(quadro);
    if (quadro == null) {
      // Era um ACK ou um quadro duplicado/descartado pelo controle de fluxo. Nao faz mais nada.
      return;
    } // fim do if
    
    quadro = retirarNumeroDeSequencia(quadro);

    // Chama a proxima camada
    camadaAplicacaoReceptora.camadaAplicacaoReceptora(quadro);
  }// fim do metodo camadaEnlaceDadosReceptora

  /* ***************************************************************
  * Metodo: camadaEnlaceDadosReceptoraEnquadramento
  * Funcao: Realiza o desenquadramento
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: Quadro desenquadrado
  *************************************************************** */
  public int[] camadaEnlaceDadosReceptoraEnquadramento (int quadro []) {
    switch (tipoDeEnquadramento) {
      case 0 : // Contagem de caracteres
        quadro = camadaEnlaceDadosReceptoraEnquadramentoContagemDeCaracteres(quadro);
        break;
      case 1 : // Insercao de bytes
        quadro = camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBytes(quadro);
        break;
      case 2 : // Insercao de bits
        quadro = camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBits(quadro);
        break;
      case 3 : // Violacao da camada fisica
        break;
    }// fim do switch/case
    return quadro;
  } // fim do metodo camadaEnlaceDadosReceptoraEnquadramento
  
  /* ***************************************************************
  * Metodo: camadaEnlaceDadosReceptoraControleDeErro
  * Funcao: Realiza o controle de erro (detectando e pedindo reenvio do quadro em caso de erro)
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: Quadro sem as informacoes de controle de erro
  *************************************************************** */
  public int[] camadaEnlaceDadosReceptoraControleDeErro (int quadro []) {
    switch (tipoDeControleDeErro) {
      case 0 : // Bit de paridade par
        quadro = camadaEnlaceDadosReceptoraControleDeErroBitDeParidadePar(quadro);
        break;
      case 1 : // Bit de paridade impar
        quadro = camadaEnlaceDadosReceptoraControleDeErroBitDeParidadeImpar(quadro);
        break;
      case 2 : // CRC
        quadro = camadaEnlaceDadosReceptoraControleDeErroCRC(quadro);
        break;  
      case 3 : // Codigo de Hamming
        quadro = camadaEnlaceDadosReceptoraControleDeErroCodigoDeHamming(quadro);
        break;
    }// fim do switch/case
    return quadro;
  } // fim do metodo camadaEnlaceDadosReceptoraControleDeErro
  
  /* ***************************************************************
  * Metodo: camadaEnlaceDadosReceptoraControleDeFluxo
  * Funcao: Realiza o controle de fluxo
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: void
  *************************************************************** */
  public int[] camadaEnlaceDadosReceptoraControleDeFluxo (int quadro []) {
    if (quadro == null) {
      return null; // Se o quadro for nulo, apenas repassa o nulo.
    } // fim do if

    switch (tipoDeControleDeFluxo) {
      case 0 : //protocolo de janela deslizante de 1 bit
        quadro = camadaEnlaceDadosReceptoraJanelaDeslizanteUmBit(quadro);
        break;
      case 1 : //protocolo de janela deslizante go-back-n
        quadro = camadaEnlaceDadosReceptoraJanelaDeslizanteGoBackN(quadro);
        break;
      case 2 : //protocolo de janela deslizante com retransmissao seletiva
        quadro = camadaEnlaceDadosReceptoraJanelaDeslizanteComRetransmissaoSeletiva(quadro);
        break; 
    }//fim do switch/case
    // Se nao era um ACK, retorna o quadro para processamento normal
    return quadro;
  } // fim do metodo camadaEnlaceDadosReceptoraControleDeFluxo

  /* ***************************************************************
  * Metodo: camadaEnlaceDadosReceptoraEnquadramentoContagemDeCaracteres
  * Funcao: Realiza o enquadramento pelo tipoDeEnquadramento de contagem de caracteres
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: O quadro original
  *************************************************************** */
  public int[] camadaEnlaceDadosReceptoraEnquadramentoContagemDeCaracteres (int quadro []) {
    if (quadro == null) 
      return null;
    // Pega os 8 bits mais a esquerda de quadro[0] (o byte de contagem)
    int tamanhoTotalDoQuadro = (quadro[0] >>> 24) & 0xFF;
    // Calcula quantos bytes de DADOS (carga util) seram lidos
    int bytesDeDadosParaLer = tamanhoTotalDoQuadro - 1;

    // Prepara o array de saida. Como a carga util tem no max 4 bytes (32 bits).
    int[] novoQuadro = new int[1];
    novoQuadro[0] = 0; // Garante que o pacote de saida comece zerado

    // Converte o numero de bytes de dados para bits
    int bitsDeDadosParaLer = bytesDeDadosParaLer * 8;

    // Ponteiro do array de entrada (comeca em quadro[0])
    int indiceIntEntrada = 0;
    // Comeca a LEITURA no bit 23 (logo apos os 8 bits do cabecalho)
    int bitPosEntrada = 23; 
    // Comeca a ESCRITA no bit 31 (o mais a esquerda do pacote de saida)
    int bitPosSaida = 31;

    // Iteracao que executa a quantidade exata de bits de dados que o cabecalho mandou
    for (int i = 0; i < bitsDeDadosParaLer; i++) {
      // Le o bit atual do quadro de ENTRADA
      int bitLido = (quadro[indiceIntEntrada] >> bitPosEntrada) & 1;
      // Se o bit lido for 1, liga o bit correspondente no quadro de SAIDA
      if (bitLido == 1) {
        novoQuadro[0] |= (1 << bitPosSaida);
      } // fim do if
      // Move os ponteiros de leitura e escrita um bit para a direita
      bitPosEntrada--;
      bitPosSaida--;
      // Se o ponteiro de LEITURA terminou um 'int'
      if (bitPosEntrada < 0) {
        // reseta ele para o comeco (bit 31)
        bitPosEntrada = 31;
        // avanca para o proximo 'int' do array de entrada (quadro[1])
        indiceIntEntrada++;
      } // fim do if
    } // fim do for
    // Retorna o pacote de dados limpo (sem o cabecalho)
    return novoQuadro;
  }// fim do metodo camadaEnlaceDadosReceptoraEnquadramentoContagemDeCaracteres
  
  /* ***************************************************************
  * Metodo: camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBytes
  * Funcao: Realiza o enquadramento pelo tipoDeEnquadramento de insercao de bits
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: O quadro original
  *************************************************************** */
  public int[] camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBytes (int quadro []) {
    if (quadro == null) 
      return null;
    final int FLAG = 126 ; // ASCII: ~
    final int ESCAPE = 125; // ASCII: }

    // O pacote de saida tera 4 bytes (1 int)
    int[] pacoteDeDados = new int[1];
    pacoteDeDados[0] = 0; // Importante: garante que comeca zerado

    // True apos a 1a FLAG, false antes da 1a e apos a 2a
    boolean bitsDeDados = false; 
    // True se o ultimo byte lido foi um ESCAPE
    boolean vimosEscape = false;
    // Contador para sabermos quando parar
    int bytesEscritos = 0;

    // Loop "byte a byte" pelo array de entrada
    for (int i = 0; i < 3; i++) {
      // Itera pelos bytes dentro de cada int
      for (int j = 3; j >= 0; j--) {
        // Le o byte atual
        int byteLido = (quadro[i] >> (j * 8)) & 0xFF;
        // Logica de Destuffing e Desenquadramento
        if (vimosEscape) {
          // O byte anterior foi ESCAPE.
          // Este byte e DADO, nao importa o que seja.
          // Logica de escrita:
          int shift = (3 - bytesEscritos) * 8;
          pacoteDeDados[0] |= (byteLido << shift);
          bytesEscritos++;

          vimosEscape = false; // Reseta a flag
        } else if (byteLido == FLAG) {
          // E uma FLAG de controle.
          if (!bitsDeDados) {
            // E a FLAG de INICIO.
            bitsDeDados = true;
          } else {
            // E a FLAG de FIM.
            bitsDeDados = false;
            break; // Para de processar. Fim do quadro.
          } // fim do if
        } else if (byteLido == ESCAPE) {
          // E um byte de ESCAPE. Ativa a flag e espera o proximo.
          vimosEscape = true;    
        } else if (bitsDeDados) {
          // E um byte de dados normal (nem FLAG, nem ESCAPE).
          
          // Logica de escrita:
          int shift = (3 - bytesEscritos) * 8;
          pacoteDeDados[0] |= (byteLido << shift);
          bytesEscritos++;
        } // fim do if
            
        // Condicao de parada
        // Se ja lemos os 4 bytes, nao precisamos nem
        // esperar a FLAG de fim.
        if (bytesEscritos >= 4) {
          break;
        } // fim do if
      } // fim do for
        
      // Se um 'break' aconteceu no loop interno (fim da flag ou 4 bytes lidos),
      // quebra o loop externo (dos ints) tambem.
      if (bytesEscritos >= 4 || !bitsDeDados && i > 0) { 
        break;
      } // fim do if
    } // fim do for
    return pacoteDeDados;
  }//fim do metodo camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBytes
  
  /* ***************************************************************
  * Metodo: camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBits
  * Funcao: realiza o desenquadramento pela insercao de bits
  * Parametros: quadro = conjunto de bits da mensagem (com stuffing e flags)
  * Retorno: O quadro original
  *************************************************************** */
  public int[] camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBits(int quadro[]) {
    if (quadro == null) 
      return null;
    // O pacote de dados que queremos extrair tem 32 bits.
    int[] pacoteDeDados = new int[1];
    pacoteDeDados[0] = 0; // Importante garantir que comeca zerado
    final int TAMANHO_PACOTE_BITS = 32;

    int contadorDeUns = 0;
    // "Ponteiro" de escrita (conta de 0 a 31)
    int bitDeSaida = 0; 

    // Loop pelo buffer de entrada (quadro), comecando apos a flag (bit 8)
    for (int i = 8; i < 64; i++) { 

      // Le o bit da entrada
      int indiceArrayEntrada = i / 32;
      int bitPosEntrada = 31 - (i % 32);
      int bitLido = (quadro[indiceArrayEntrada] >> bitPosEntrada) & 1;

      // Logica de Destuffing (Verifica se e um '0' inserido)
      if (contadorDeUns == 5 && bitLido == 0) {
        contadorDeUns = 0;
        continue; // Pula para o proximo bit do loop 'for'
      } // fim do if

      // Se NAO for stuffing, e um bit de dado. Salva ele.
      if (bitLido == 1) {
        int bitPosSaida = 31 - (bitDeSaida % 32);
        pacoteDeDados[0] |= (1 << bitPosSaida);
      } // fim do if
      // Se bitLido for 0, nao precisa fazer nada, pois ja e 0

      // Atualiza o contador de '1's para o proximo bit
      if (bitLido == 1) {
        contadorDeUns++;
      } else {
        contadorDeUns = 0;
      } // fim do if
      // Avanca o ponteiro de escrita
      bitDeSaida++;
      // Verifica se ja lemos o pacote inteiro
      if (bitDeSaida >= TAMANHO_PACOTE_BITS) {
        break; // Ignora o resto.
      } // fim do if
    } // fim do for
    return pacoteDeDados;
  } // fim do metodo camadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBits
  
  /* ***************************************************************
  * Metodo: camadaEnlaceDadosReceptoraControleDeErroBitDeParidadePar
  * Funcao: verifica o bit de paridade par no ultimo bit do quadro e o retira
  * Parametros: quadro = conjunto menor de bits da mensagem
  * Retorno: int[] = quadro original
  *************************************************************** */
  public int[] camadaEnlaceDadosReceptoraControleDeErroBitDeParidadePar (int quadro []) {
    // Variavel para trocar entre (0 ou 1) enquanto conta os bits '1'
    int paridadeCalculada = 0;
    // Ponteiro para qual indice do array esta
    int indiceBits = 0;
    // O indice do ultimo bit
    int posicaoBitParidade = (quadro.length*32)-1;

    // Iteracao para calcular a paridade de todos os bits, exceto o ultimo.
    for (int contadorBit = 0; contadorBit < posicaoBitParidade; contadorBit++) {
      // Avanca o indice do quadro
      if (contadorBit > 0 && contadorBit % 32 == 0) {
        indiceBits++;
      } // fim do if

      // Le o bit
      int bit = (quadro[indiceBits] >> (31 - (contadorBit % 32))) & 1;

      // Se o bit for 1, inverte o calculador
      if (bit == 1) {
        paridadeCalculada = (paridadeCalculada + 1) % 2;
      } // fim do if
    } // fim do for
    
    // Pega o indice do array
    int indiceDoBitParidade = quadro.length-1;
    // Le o bit de paridade que o transmissor enviou.
    int paridadeRecebida = (quadro[indiceDoBitParidade]) & 1;

    // Compara o que foi calculado (paridadeCalculada) com o que foi recebido (paridadeRecebida)
    if (paridadeCalculada != paridadeRecebida) {
      erroNoQuadro = true;
    } // fim do if

    // Zera o bit de paridade
    quadro[indiceDoBitParidade] &= ~(1);

    return quadro;
  } // fim do metodo camadaEnlaceDadosReceptoraControleDeErroBitDeParidadePar

  /* ***************************************************************
  * Metodo: camadaEnlaceDadosReceptoraControleDeErroBitDeParidadeImpar
  * Funcao: verifica o bit de paridade impar no ultimo bit do quadro e o retira
  * Parametros: quadro = conjunto menor de bits da mensagem
  * Retorno: int[] = quadro original
  *************************************************************** */
  public int[] camadaEnlaceDadosReceptoraControleDeErroBitDeParidadeImpar (int quadro []) {
    // Variavel para trocar entre (0 ou 1) enquanto conta os bits '1'
    int paridadeCalculada = 1;
    // Ponteiro para qual indice do array esta
    int indiceBits = 0;
    // O indice do ultimo bit
    int posicaoBitParidade = (quadro.length*32)-1;

    // Iteracao para calcular a paridade de todos os bits, exceto o ultimo.
    for (int contadorBit = 0; contadorBit < posicaoBitParidade; contadorBit++) {
      // Avanca o indice do quadro
      if (contadorBit > 0 && contadorBit % 32 == 0) {
        indiceBits++;
      } // fim do if

      // Le o bit
      int bit = (quadro[indiceBits] >> (31 - (contadorBit % 32))) & 1;

      // Se o bit for 1, inverte o calculador
      if (bit == 1) {
        paridadeCalculada = (paridadeCalculada + 1) % 2;
      } // fim do if
    } // fim do for
    
    // Pega o indice do array
    int indiceDoBitParidade = quadro.length-1;
    // Le o bit de paridade que o transmissor enviou.
    int paridadeRecebida = (quadro[indiceDoBitParidade]) & 1;

    // Compara o que foi calculado (paridadeCalculada) com o que foi recebido (paridadeRecebida)
    if (paridadeCalculada != paridadeRecebida) {
      erroNoQuadro = true;
    } // fim do if

    // Zera o bit de paridade
    quadro[indiceDoBitParidade] &= ~(1);

    return quadro;
  } // fim do metodo camadaEnlaceDadosReceptoraControleDeErroBitDeParidadeImpar

  public int[] camadaEnlaceDadosReceptoraControleDeErroCRC(int quadroComCRC[]) {
    // Tamanho do quadro original
    int tamanhoDados = quadroComCRC.length - 1;
    
    // O CRC que o transmissor enviou esta no ultimo int
    int crcRecebido = quadroComCRC[tamanhoDados];

    // Registro do CRC. Comeca com tudo 1
    int crcCalculado = 0xFFFFFFFF;

    // Itera byte a byte pelos DADOS (ignora o ultimo int)
    for (int i = 0; i < tamanhoDados; i++) {
      for (int j = 3; j >= 0; j--) {
        int umByte = (quadroComCRC[i] >> (j * 8)) & 0xFF;
        // No Receptor, TEM QUE FICAR ASSIM:
        int indice = (crcCalculado ^ umByte) & 0xFF;
        crcCalculado = (crcCalculado >>> 8) ^ TabelaCRC.getCrc32Tabela()[indice];
      } // fim do for
    } // fim do for
    
    // Finaliza o calculo
    crcCalculado = crcCalculado ^ 0xFFFFFFFF;

    // Verifica se o quadro esta certo
    if (crcCalculado != crcRecebido) {
      // Deu erro
      erroNoQuadro = true;
    }
    int[] quadroDeDados = new int[tamanhoDados];
    System.arraycopy(quadroComCRC, 0, quadroDeDados, 0, tamanhoDados);
    return quadroDeDados;
  } // fim do metodo camadaEnlaceDadosReceptoraControleDeErroCRC

  /* ***************************************************************
  * Metodo: camadaEnlaceDadosReceptoraControleDeErroCodigoDeHamming
  * Funcao: Realiza a verificacao de erro a partir do codigo de hamming
  * Parametros: quadro = unidade de transmissao recebido pelo host
  * Retorno: int[] = quadro original
  *************************************************************** */
  public int[] camadaEnlaceDadosReceptoraControleDeErroCodigoDeHamming(int[] quadro) {
    // Quantidade total de bits no quadro
    int quantidadeTotalDeBits = quadro.length*32;
    // Quantidade de bits de verificacao
    int r = 1;
    
    // Iteracao para descobrir a quantidade de bits de verificacao necessaria
    while (!((quantidadeTotalDeBits+r+1) <= (1 << r))) {
      r++;
    } // fim do while

    int codigoDeErro = 0;

    // Iteracao para calcular os bits de verificacao
    for (int i = 0; i < r; i++) {
      // Posicao dos bits de paridade
      int posParidade = 1 << i;
      // Calcula se a paridade esta cert, ou seja, se ocorreu um erro na transmissao
      int paridadeCalculada = 0;

      // Iteracao para le todos os bits do quadro
      for (int j = 1; j <= quantidadeTotalDeBits; j++) {
        // Verifica se o bit 'j' pertence ao grupo de paridade do bit de verificacao
        if ((j & posParidade) != 0) {
          // Ponteiros de leitura
          int indiceArrayLeitura = (j - 1) / 32;
          int posBitLeitura = 31 - ((j - 1) % 32);

          // Protecao contra crash (caso o array seja menor)
          if (indiceArrayLeitura >= quadro.length) continue; 
          
          // Le o bit
          int bit = (quadro[indiceArrayLeitura] >> posBitLeitura) & 1;
          // Acumula o XOR
          paridadeCalculada = paridadeCalculada ^ bit;
        } // fim do if 
      } // fim do for
        
      // Se a paridade for 1, significa que este grupo tem um erro.
      if (paridadeCalculada == 1) {
        // Adicionamos o "peso" deste bit de paridade ao codigo de erro.
        codigoDeErro += posParidade; 
      } // fim do if
      // Se for 0, o grupo esta OK, nao fazemos nada.

    } // fim do for

    if (codigoDeErro != 0) {
      quadro[((codigoDeErro - 1)/32)] ^= 1 << (31 - ((codigoDeErro - 1) % 32));
    }

    int[] quadroOriginal = new int[(quantidadeTotalDeBits-r+31)/32];
    int indiceEntrada = 0;
    int indiceSaida = 0;
    // Ponteiro de escrita de escrita (quantos dados ja foram escritos)
    int contadorBitSaida = 0;

    // Iteracao que ira LER todos os bits do quadro Hamming
    for (int contadorBit=0; contadorBit < quantidadeTotalDeBits; contadorBit++) {
      // Pega a posicao do bit que esta lendo
      int posicaoAtual = contadorBit + 1;
      // Checa se o bit eh um bit de paridade
      boolean eBitDeParidade = ((posicaoAtual & (posicaoAtual - 1)) == 0);

      // Se for bit de paridade ele e ignorado e o loop continua
      if (eBitDeParidade) {
        continue;
      } // fim do if

      // Avanca o ponteiro de leitura se necessario
      if (contadorBit % 32 == 0 && contadorBit != 0) {
        indiceEntrada++;
      } // fim do if

      // Le o bit
      int bit = (quadro[indiceEntrada] >>> 31 - (contadorBit % 32)) & 1;

      // Avanca o ponteiro de escrita se necessario
      if (contadorBitSaida % 32 == 0 && contadorBitSaida != 0) {
        indiceSaida++;
      } // fim do if

      // Transfere o bit de dado para o quadro de saida
      quadroOriginal[indiceSaida] |= bit << (31 - (contadorBitSaida % 32));

      // Avanca o contador de bits que foi escrito
      contadorBitSaida++;
    } // fim do for

    return quadroOriginal;
  } // fim do metodo camadaEnlaceDadosReceptoraControleDeErroCodigoDeHamming

  /* ***************************************************************
  * Metodo: camadaEnlaceDadosReceptoraJanelaDeslizanteUmBit
  * Funcao: realiza o controle de fluxo pelo metodo da janela deslizante de um bit
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: int[]
  *************************************************************** */
  public int[] camadaEnlaceDadosReceptoraJanelaDeslizanteUmBit (int quadro []) {
    
    int bitVerificacaoAck = quadro[0] >>> 31;

    // Se for um ACK, notifica o transmissor deste HOST
    if (bitVerificacaoAck == 1) {
      System.out.println("Receptor: ACK detectado.");
      if (meuTransmissor != null) {
        // Notifica o transmissor do *nosso* host que o ACK chegou
        meuTransmissor.receberAck(quadro); // Passa o quadro ACK
      } // fim do if
      return null; // Retorna nulo para sinalizar que era um ACK
    } // fim do if
    
    // Extrai o numero de sequencia do quadro (bits 30-24).
    int nsRecebido = (quadro[0] >>> 24) & 0x7F; // Extrai os 7 bits de NS

    System.out.println("Receptor: Quadro DADOS detectado (NS=" + nsRecebido + " vs Esperado=" + nsEsperado + ")");

    if (nsRecebido == nsEsperado) {
      // Quadro e o esperado.
      nsEsperado = (nsEsperado + 1) % 8;
      
      System.out.println("Receptor: Quadro OK. Enviando ACK para #" + nsEsperado);
      
      meuTransmissor.enviarAck(nsEsperado); 
        
      return quadro;

    } else {
      System.out.println("Receptor: Quadro errado (NS=" + nsRecebido + "). Descartando e re-enviando ACK para #" + nsEsperado);
        
      meuTransmissor.enviarAck(nsEsperado); 
        
      return null;
    } // fim do if
  }//fim do metodo camadaEnlaceDadosReceptoraJanelaDeslizanteUmBit
  
  /* ***************************************************************
  * Metodo: camadaEnlaceDadosReceptoraJanelaDeslizanteGoBackN
  * Funcao: realiza o controle de fluxo pelo metodo da janela deslizante go and back n
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: int[]
  *************************************************************** */
  public int[] camadaEnlaceDadosReceptoraJanelaDeslizanteGoBackN (int quadro []) {
    int bitVerificacaoAck = quadro[0] >>> 31;

    // Se for um ACK, notifica o transmissor deste HOST (mesma logica do S/W)
    if (bitVerificacaoAck == 1) {
      System.out.println("Receptor GBN: ACK detectado.");
      if (meuTransmissor != null) {
        meuTransmissor.receberAck(quadro); 
      } // fim do if
      return null; // Era um ACK
    } // fim do if
    
    // --- Logica GBN para DADOS ---
    int nsRecebido = (quadro[0] >>> 24) & 0x7F; // Extrai o NS

    System.out.println("Receptor GBN: Quadro DADOS detectado (NS=" + nsRecebido + " vs Esperado=" + nsEsperado + ")");

    if (nsRecebido == nsEsperado) {
      // 1. Quadro e o esperado (em ordem). Aceita e avança.
      System.out.println("Receptor GBN: Quadro OK. Enviando ACK para #" + (nsEsperado + 1) % ESPACO_SEQUENCIA);
      
      nsEsperado = (nsEsperado + 1) % ESPACO_SEQUENCIA; // Avança para o proximo
      
      // 2. Envia ACK cumulativo (NR = nsEsperado)
      meuTransmissor.enviarAck(nsEsperado); 
        
      return quadro;

    } else {
      // 3. Quadro fora de ordem ou duplicado. Descarta e re-envia ACK do uLTIMO OK.
      System.out.println("Receptor GBN: Quadro fora de ordem (NS=" + nsRecebido + "). Descartando e re-enviando ACK para #" + nsEsperado);
        
      // Envia ACK para o nsEsperado atual (informa o transmissor que o quadro NS=nsEsperado e o que falta)
      meuTransmissor.enviarAck(nsEsperado); 
        
      return null; // Descarta o quadro
    } // fim do if
  }//fim do metodo camadaEnlaceDadosReceptoraJanelaDeslizanteGoBackN
  
  /* ***************************************************************
  * Metodo: camadaEnlaceDadosReceptoraJanelaDeslizanteComRetransmissaoSeletiva
  * Funcao: realiza o controle de fluxo pelo metodo da janela com retransmissao seletiva
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: int[]
  *************************************************************** */
  public int[] camadaEnlaceDadosReceptoraJanelaDeslizanteComRetransmissaoSeletiva (int quadro []) {
    int bitVerificacaoAck = quadro[0] >>> 31;

    // Se for um ACK, notifica o transmissor deste HOST
    if (bitVerificacaoAck == 1) {
      System.out.println("Receptor SR: ACK detectado.");
      if (meuTransmissor != null) {
        meuTransmissor.receberAck(quadro); 
      } // fim do if
      return null; // Era um ACK
    } // fim do if
    
    // NS = Numero de Sequência do quadro de dados recebido
    int nsRecebido = (quadro[0] >>> 24) & 0x7F;

    System.out.println("Receptor SR: Quadro DADOS detectado (NS=" + nsRecebido + " vs Esperado=" + nsEsperado + ")");

    // Limites da Janela Receptora: [nsEsperado (base), nsEsperado + W)
    int limiteSuperior = (nsEsperado + TAMANHO_JANELA) % ESPACO_SEQUENCIA; 

    // Checa se o NS recebido esta na Janela Receptora (logica circular)
    boolean estaNaJanela = false;
    if (nsEsperado <= limiteSuperior) {
      // Janela nao circulou: [nsEsperado, limiteSuperior)
      estaNaJanela = nsRecebido >= nsEsperado && nsRecebido < limiteSuperior;
    } else {
      // Janela circulou: [nsEsperado, ESPACO_SEQUENCIA) U [0, limiteSuperior)
      estaNaJanela = nsRecebido >= nsEsperado || nsRecebido < limiteSuperior;
    }
    
    // Calcula o limite inferior da janela de aceitaçao para quadros que ja foram recebidos
    int limiteInferior = (nsEsperado - TAMANHO_JANELA + ESPACO_SEQUENCIA) % ESPACO_SEQUENCIA;

    // Verifica se o quadro esta na Janela Receptora (para aceitaçao)
    if (estaNaJanela) {
        
      int indiceBuffer = nsRecebido % TAMANHO_JANELA;
        
      // 1. Envia ACK para o quadro recebido (NS+1 = NR)
      int nrAEnviar = (nsRecebido + 1) % ESPACO_SEQUENCIA;
      System.out.println("Receptor SR: Quadro OK. Enviando ACK para #" + nrAEnviar);
      meuTransmissor.enviarAck(nrAEnviar);
        
      // 2. Armazena o quadro (se nao for duplicado)
      if (bufferReceptor[indiceBuffer] == null) {
        bufferReceptor[indiceBuffer] = quadro;
        System.out.println("Receptor SR: Quadro #" + nsRecebido + " armazenado no buffer.");
      } else {
        System.out.println("Receptor SR: Quadro duplicado #" + nsRecebido + ". Descartado (ja no buffer).");
      }
        
      // 3. Verifica se o quadro recebido e a BASE (nsEsperado)
      if (nsRecebido == nsEsperado) {
        int quadroADevolver[] = null;
            
        // Entrega os quadros em ordem e avança a janela
        while (bufferReceptor[nsEsperado % TAMANHO_JANELA] != null) {
                
        // O quadro a ser devolvido para a proxima camada e o primeiro na sequência
        quadroADevolver = bufferReceptor[nsEsperado % TAMANHO_JANELA];
        System.out.println("Receptor SR: Entregando quadro #" + nsEsperado + " e deslizando janela.");

        // Limpa o slot do buffer
        bufferReceptor[nsEsperado % TAMANHO_JANELA] = null; 
                
        // Avança a base
        nsEsperado = (nsEsperado + 1) % ESPACO_SEQUENCIA;
      } // fim do while

      return quadroADevolver; // Retorna o ultimo quadro entregue
            
      } else {
        // Quadro fora de ordem. Ja enviamos o ACK e armazenamos. Nada a entregar agora.
        System.out.println("Receptor SR: Quadro fora de ordem #" + nsRecebido + ". Armazenado.");
        return null;
      }

    } else {
      // Quadro fora da janela (muito antigo ou muito a frente).
      // Se for muito antigo (limiteInferior <= NS < nsEsperado), re-envia o ACK para NS+1
      if (meuTransmissor.estaDentroDaJanela(nsRecebido, limiteInferior, nsEsperado)) {
        int nrAEnviar = (nsRecebido + 1) % ESPACO_SEQUENCIA;
        System.out.println("Receptor SR: Quadro antigo/duplicado, re-enviando ACK para #" + nrAEnviar);
        meuTransmissor.enviarAck(nrAEnviar);
      } else {
        // Muito a frente
        System.out.println("Receptor SR: Quadro muito fora da janela. Descartando.");
      } // fim do if
      return null; 
    } // fim do if
  }//fim do camadaEnlaceDadosReceptoraJanelaDeslizanteComRetransmissaoSeletiva
  
  private int[] retirarNumeroDeSequencia(int[] quadro) {
    
    int[] quadroSemSequencia = new int[1];
    
    quadroSemSequencia[0] = quadro[0] << 8;

    return quadroSemSequencia;
  }

}