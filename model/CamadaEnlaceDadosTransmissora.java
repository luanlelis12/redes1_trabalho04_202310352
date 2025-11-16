/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 26 09 2025
* Ultima alteracao.: 02 11 2025
* Nome.............: CamadaEnlaceDadosTransmissora.java
* Funcao...........: simula a camada de enlace de dados transmissora
*************************************************************** */

package model;

import util.ConversorStringBinario;
import util.TabelaCRC;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CamadaEnlaceDadosTransmissora {

  private CamadaFisicaTransmissora camadaFisicaTransmissora;

  private int tipoDeEnquadramento = 0;
  private int tipoDeControleDeErro = 0;
  private int tipoDeControleDeFluxo = 0;

  private int numDeSequencia = -1;

  private volatile boolean esperandoAck = false;
  private final Object lock = new Object();
  private static final long TIMEOUT_MS = 1000; // 1 segundos de timeout

  // Executor para agendar a tarefa de timeout
  private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private ScheduledFuture<?> timerHandle;

  /* ***************************************************************
  * Metodo: CamadaEnlaceDadosTransmissora
  * Funcao: define a camada de enlace transmissora do mesmo host
  * Parametros: camadaFisicaTransmissora = camada de enlace transmissora
  * Retorno: void
  *************************************************************** */
  public CamadaEnlaceDadosTransmissora(CamadaFisicaTransmissora camadaFisicaTransmissora) {
    this.camadaFisicaTransmissora = camadaFisicaTransmissora;
  } // fim do metodo CamadaEnlaceDadosTransmissora

  /* ***************************************************************
  * Metodo: setTipoDeEnquadramento
  * Funcao: define o tipo de tipoDeEnquadramento que sera usada
  * Parametros: tipoDeEnquadramento = define um numero inteiro que representa um tipo de enquadramento
  * Retorno: void
  *************************************************************** */
  public void setTipoDeEnquadramento (int tipoDeEnquadramento) {
    this.tipoDeEnquadramento = tipoDeEnquadramento;
  } // fim do metodo setTipoDeEnquadramento

  /* ***************************************************************
  * Metodo: setTipoDeControleDeErro
  * Funcao: define o tipo de tipo de controle de erro
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
  * Metodo: camadaEnlaceDadosTransmissora
  * Funcao: executa todas as partes da camada de enlace
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: void
  *************************************************************** */
  public void camadaEnlaceDadosTransmissora(int quadro[]) {

    quadro = inserirNumeroDeSequencia(quadro);
    quadro = camadaEnlaceDadosTransmissoraEnquadramento(quadro);
    quadro = camadaEnlaceDadosTransmissoraControleDeErro(quadro);
    ConversorStringBinario.exibirBits(quadro);
    // camadaEnlaceDadosTransmissoraControleDeFluxo(quadro);
    camadaFisicaTransmissora.camadaFisicaTransmissora(quadro);

  }// fim do metodo camadaEnlaceDadosTransmissora

  /* ***************************************************************
  * Metodo: camadaEnlaceDadosTransmissoraEnquadramento
  * Funcao: realiza o enquadramento
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: void
  *************************************************************** */
  public int[] camadaEnlaceDadosTransmissoraEnquadramento (int quadro []) {
    switch (tipoDeEnquadramento) {
      case 0 : //contagem de caracteres
        quadro = camadaEnlaceDadosTransmissoraEnquadramentoContagemDeCaracteres(quadro);
        break;
      case 1 : //insercao de bytes
        quadro = camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBytes(quadro);
        break;
      case 2 : //insercao de bits
        quadro = camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBits(quadro);
        break;
      case 3 : //violacao da camada fisica
        // camadaDeEnlaceTransmissoraEnquadramentoViolacaoCamadaFisica(quadro);
        break;
    }//fim do switch/case
    return quadro;
  } //fim do metodo camadaEnlaceDadosTransmissoraEnquadramento
  
  /* ***************************************************************
  * Metodo: camadaEnlaceDadosTransmissoraControleDeErro
  * Funcao: realiza o controle de erro
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: void
  *************************************************************** */
  public int[] camadaEnlaceDadosTransmissoraControleDeErro (int quadro []) {
    switch (tipoDeControleDeErro) {
      case 0 : //bit de paridade par
        quadro = camadaEnlaceDadosTransmissoraControleDeErroBitParidadePar(quadro);
        break;
      case 1 : //bit de paridade impar
        quadro = camadaEnlaceDadosTransmissoraControleDeErroBitParidadeImpar(quadro);
        break;
      case 2 : //CRC
        quadro = camadaEnlaceDadosTransmissoraControleDeErroCRC(quadro);
        break;  
      case 3 : //codigo de Hamming
        quadro = camadaEnlaceDadosTransmissoraControleDeErroCodigoDeHamming(quadro);
        break;
    }//fim do switch/case    
    return quadro;
  } //fim do metodo camadaEnlaceDadosTransmissoraControleDeErro
  
  /* ***************************************************************
  * Metodo: camadaEnlaceDadosTransmissoraControleDeFluxo
  * Funcao: Faz o envio dos ACKs e inicializa o temporizador
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: void
  *************************************************************** */
  public void camadaEnlaceDadosTransmissoraControleDeFluxo (int quadro []) {
    switch (tipoDeControleDeErro) {
      case 0 : //protocolo de janela deslizante de 1 bit
        //codigo
        break;
      case 1 : //protocolo de janela deslizante go-back-n
        //codigo
        break;
      case 2 : //protocolo de janela deslizante com retransmissão seletiva
        //codigo
        break; 
    }//fim do switch/case

    // synchronized (lock) {
    //   // Se ja estiver esperando um ACK, a camada de aplicacao ficara bloqueada aqui
    //   while (esperandoAck) {
    //     try {
    //       System.out.println("Transmissor: Bloqueado. Esperando ACK anterior...");
    //       lock.wait();
    //     } catch (InterruptedException e) {
    //       Thread.currentThread().interrupt();
    //       return;
    //     } // fim do try/catch
    //   } // fim do while

    //   // Marca que esperando um ack
    //   this.esperandoAck = true;
    //   System.out.println("Transmissor: Iniciando envio do quadro.");
      
    //   // Envia os dados e inicia o timer
    //   enviarEIniciarTimer(quadro);

    //   // Espera pelo ACK e a thread da aplicacao fica parada aqui
    //   while (esperandoAck) {
    //     try {
    //       // Espera ser notificado pelo ACK ou pelo timeout
    //       lock.wait();
    //     } catch (InterruptedException e) {
    //       Thread.currentThread().interrupt();
    //       break;
    //     } // fim do try/catch
    //   } // fim do while

    //   // Quando sair do loop, ou o ACK chegou ou o envio foi interrompido
    //   System.out.println("Transmissor: Envio concluido. Desbloqueando aplicacao.");
    // }
  } //fim do metodo camadaEnlaceDadosTransmissoraControleDeFluxo

  /* ***************************************************************
  * Metodo: camadaEnlaceDadosTransmissoraEnquadramentoContagemDeCaracteres
  * Funcao: realiza o enquadramento pelo tipoDeEnquadramento de contagem de caracteres
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: void
  *************************************************************** */
  public int[] camadaEnlaceDadosTransmissoraEnquadramentoContagemDeCaracteres (int quadro []) {
    int[] quadroEnquadrado = new int[2]; // Buffer de saida (max 5 bytes)
    int pacoteDeDados = quadro[0];     // Pega o int[0] com os 4 bytes de dados

    int numeroDeBytesDeDados = 0;
    
    // Itera pelos 4 bytes do pacote, da esquerda (j=3) para a direita (j=0)
    for (int j = 3; j >= 0; j--) {
      int byteAtual = (pacoteDeDados >> (j * 8)) & 0xFF;
      if (byteAtual == 0 & j != 3) {
        // Encontrou o primeiro '0', para de contar.
        break;
      } else {
        numeroDeBytesDeDados++;
      } // fim do if
    } // fim do for

    // O byte de cabecalho e a contagem de dados + 1
    int byteDeContagem = numeroDeBytesDeDados + 1;
    // Coloca o byte de contagem nos 8 bits mais a esquerda
    quadroEnquadrado[0] = byteDeContagem << 24;
    // Copia os 3 primeiros bytes do pacote
    quadroEnquadrado[0] |= (pacoteDeDados >>> 8); 
    // Copia o 4 byte do pacote
    quadroEnquadrado[1] = (pacoteDeDados & 0xFF) << 24;

    // Retorna o quadro enquadrado
    return quadroEnquadrado;
  }//fim do metodo camadaEnlaceDadosTransmissoraEnquadramentoContagemDeCaracteres
  
  /* ***************************************************************
  * Metodo: camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBytes
  * Funcao: realiza o enquadramento pelo tipoDeEnquadramento de insercao de bits
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: void
  *************************************************************** */
    public int[] camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBytes (int quadro []) {
      final int FLAG = 126 ; // ASCII: ~
      final int ESCAPE = 125; // ASCII: }

      // O pior caso cabe em um int[3] (12 bytes).
      int[] quadroStuffed = new int[3];

      // Ponteiro do array de entrada (comeca em quadro[0])
      int indiceIntSaida = 0;
      // Em qual byte dentro do int
      int posByteNoInt = 0;   

      // Escreve a FLAG inicial
      quadroStuffed[indiceIntSaida] |= (FLAG << (8 * (3 - posByteNoInt)));
      posByteNoInt++; // Avanca para o proximo byte (posicao 1)

      // Itera sobre os 4 bytes do PACOTE de entrada (quadro[0])
      for (int i = 3; i >= 0; i--) {
        // Pega um byte do pacote original
        int caractere = (quadro[0] >> (i * 8)) & 0xFF;

        // Verifica se o byte precisa de "stuffing" (escape)
        if (caractere == FLAG || caractere == ESCAPE) {
          // Escreve o byte de ESCAPE
          quadroStuffed[indiceIntSaida] |= (ESCAPE << (8 * (3 - posByteNoInt)));
          posByteNoInt++;
          // Verifica se preencheu o int e precisa pular para o proximo
          if (posByteNoInt > 3) {
            posByteNoInt = 0;
            indiceIntSaida++;
          } // fim do if

          // Escreve o caractere original
          quadroStuffed[indiceIntSaida] |= (caractere << (8 * (3 - posByteNoInt)));
          posByteNoInt++;
          // Verifica se pulou para o proximo int
          if (posByteNoInt > 3) {
            posByteNoInt = 0;
            indiceIntSaida++;
          } // fim do if

        } else {        
          // Escreve o caractere normal (sem stuffing)
          quadroStuffed[indiceIntSaida] |= (caractere << (8 * (3 - posByteNoInt)));
          posByteNoInt++;
          // Verifica se pulou para o proximo int
          if (posByteNoInt > 3) {
            posByteNoInt = 0;
            indiceIntSaida++;
          } // fim do if
        } // fim do if
      } // fim do for
      
      // Escreve a FLAG final
      quadroStuffed[indiceIntSaida] |= (FLAG << (8 * (3 - posByteNoInt)));
      // Retorna o quadro preenchido
      return quadroStuffed;
    }//fim do metodo camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBytes
  
  /* ***************************************************************
  * Metodo: camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBits
  * Funcao: realiza o enquadramento pelo tipoDeEnquadramento de insercao de bits
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: int[]
  *************************************************************** */
  public int[] camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBits (int quadro []) {
    
    final int FLAG = 126; // 01111110
    final int TAMANHO_DADOS_BITS = 32; // 4 bytes do quadro anterior

    // Array criado pensando no pior caso
    int[] quadroEnquadrado = new int[2]; // 64 bits, inicializados com 0
    
    // bitDeSaida e contadorDeUns para verificar a necesidade de um escape de escrita. Conta de 0 a 63.
    int bitDeSaida = 0; 
    int contadorDeUns = 0;

    // Adiciona a FLAG de inicio
    for (int i = 7; i >= 0; i--) {
      int bit = (FLAG >> i) & 1;
        
      // Logica para escrever 'bit' na posicao 'bitDeSaida'
      if (bit == 1) {
        int indiceArray = bitDeSaida / 32; // Em qual int (0 ou 1)
        int bitPos = 31 - (bitDeSaida % 32); // Em qual bit (31 a 0)
        quadroEnquadrado[indiceArray] |= (1 << bitPos);
      } // fim do if
      bitDeSaida++;
    } // fim do for

    // Adiciona os DADOS (40 bits) com stuffing
    for (int i = 0; i < TAMANHO_DADOS_BITS; i++) {
      // Le o bit 'i' do quadro de entrada
      int indiceArrayEntrada = i / 32;
      int bitPosEntrada = 31 - (i % 32);
      int bit = (quadro[indiceArrayEntrada] >> bitPosEntrada) & 1;

      // Escreve o bit de dados na saida
      if (bit == 1) {
        int indiceArray = bitDeSaida / 32;
        int bitPos = 31 - (bitDeSaida % 32);
        quadroEnquadrado[indiceArray] |= (1 << bitPos);
      } // fim do if
      bitDeSaida++;

      // Logica de Stuffing
      if (bit == 1) {
        contadorDeUns++;
        if (contadorDeUns == 5) {
          // Encontrou '11111', insere um '0'.
          // Como o array ja e 0, so precisamos avancar o ponteiro.
          bitDeSaida++; 
          // E reseta o contador
          contadorDeUns = 0;
        } // fim do if
      } else {
        // O bit e '0', reseta o contador
        contadorDeUns = 0;
      } // fim do if
    } // fim do for

    // --- 3. Adiciona a FLAG de fim (8 bits) ---
    for (int i = 7; i >= 0; i--) {
      // Garante que nao vamos estourar o array de 64 bits
      if (bitDeSaida >= 64) {
        break; // Ocorreu o pior caso, ja enchemos os 64 bits.
      } // fim do if
        
      int bit = (FLAG >> i) & 1;
        
      if (bit == 1) {
        int indiceArray = bitDeSaida / 32;
        int bitPos = 31 - (bitDeSaida % 32);
        quadroEnquadrado[indiceArray] |= (1 << bitPos);
      } // fim do if
      bitDeSaida++;
    } // fim do for

    // Retorna o novo quadro. Os bits nao utilizados no final
    // de quadroEnquadrado[1] serao '0', o que e inofensivo.
    return quadroEnquadrado;
  }//fim do metodo camadaEnlaceDadosTransmissoraEnquadramentoInsercaoDeBits
  
  /* ***************************************************************
  * Metodo: camadaEnlaceDadosTransmissoraControleDeErroBitParidadePar
  * Funcao: inseri um bit de paridade par no ultimo bit do quadro
  * Parametros: quadro = unidade de transmissao que subdivide a mensagem
  * Retorno: int[] = quadro com o bit de paridade
  *************************************************************** */
  public int[] camadaEnlaceDadosTransmissoraControleDeErroBitParidadePar (int quadro []) {
    // Calcula o numero total de bits no buffer
    int totalBitsNoBuffer = quadro.length * 32;
    // Sera usado todos os bits, exceto o ultimo, para calcular a paridade.
    int bitsDeDados = totalBitsNoBuffer - 1;

    // Onde calcula a paridade (0 ou 1)
    int bitDeParidade = 0;
    // Em qual indice esta lendo
    int indiceInt = 0; 

    for (int contadorBit = 0; contadorBit <= bitsDeDados; contadorBit++) {
      // Se acabar de ler o ultimo bit de indice, avanca
      if (contadorBit%32==0 & contadorBit!=0) {
        indiceInt++;
      } // fim do if
      
      // Le o bit
      int bit = (quadro[indiceInt] >> 31-(contadorBit%32)) & 1;

      // Se o bit for 1, inverto o calculador de paridade.
      if (bit == 1) {
        bitDeParidade=(bitDeParidade+1)%2;
      } // fim do if

    } // fim do for

    // Calcula a posicao exata do ultimo bit
    int indiceBitParidade = (bitsDeDados) / 32;
    int posNoInt = 31 - (bitsDeDados % 32);

    if (bitDeParidade == 1) {
      // Insere o bit 1 no final
      quadro[indiceBitParidade] |= (1 << posNoInt);
    } else {
      // Insere o bit 0 no final
      quadro[indiceBitParidade] &= ~(1 << posNoInt);
    } // fim do if

    return quadro;
  } //fim do metodo camadaEnlaceDadosTransmissoraControleDeErroBitParidadePar

  /* ***************************************************************
  * Metodo: camadaEnlaceDadosTransmissoraControleDeErroBitParidadeImpar
  * Funcao: inseri um bit de paridade impar no ultimo bit do quadro
  * Parametros: quadro = unidade de transmissao que subdivide a mensagem
  * Retorno: int[] = quadro com o bit de paridade
  *************************************************************** */
  public int[] camadaEnlaceDadosTransmissoraControleDeErroBitParidadeImpar (int quadro []) {
    // Calcula o numero total de bits no buffer
    int totalBitsNoBuffer = quadro.length * 32;
    // Sera usado todos os bits, exceto o ultimo, para calcular a paridade.
    int bitsDeDados = totalBitsNoBuffer - 1;

    // Onde calcula a paridade (0 ou 1)
    int bitDeParidade = 1;
    // Em qual indice esta lendo
    int indiceInt = 0; 

    for (int contadorBit = 0; contadorBit <= bitsDeDados; contadorBit++) {
      // Se acabar de ler o ultimo bit de indice, avanca
      if (contadorBit%32==0 & contadorBit!=0) {
        indiceInt++;
      } // fim do if

      // Le o bit do quadro
      int bit = (quadro[indiceInt] >> 31-(contadorBit%32)) & 1;

      // Se o bit for 1, inverto o calculador de paridade.
      if (bit == 1) {
        bitDeParidade=(bitDeParidade+1)%2;
      } // fim do if

    } // fim do for

    // Calcula a posicao exata do ultimo bit
    int indiceBitParidade = (bitsDeDados) / 32;
    int posNoInt = 31 - (bitsDeDados % 32);

    if (bitDeParidade == 1) {
      // Insere o bit 1 no final
      quadro[indiceBitParidade] |= (1 << posNoInt);
    } else {
      // Insere o bit 0 no final
      quadro[indiceBitParidade] &= ~(1 << posNoInt);
    } // fim do if

    return quadro;
  } //fim do metodo camadaEnlaceDadosTransmissoraControleDeErroBitParidadeImpar

  /* ***************************************************************
  * Metodo: camadaEnlaceDadosTransmissoraControleDeErroCRC
  * Funcao: implementa o protocolo de ErroCRC
  * Parametros: quadro = unidade de transmissao que subdivide a mensagem
  * Retorno: int[] = bits apos inserir a informacao de controle
  *************************************************************** */
  public int[] camadaEnlaceDadosTransmissoraControleDeErroCRC (int quadro []) {

    // Quantidade total de bits do quadro original
    int totalBits = quadro.length*32;
    // Quantidade total de bits do quadro quando o crc (resto)
    int totalBitsComCRC = totalBits+32;
    // Array que armazenara o novo quadro
    int[] quadroComCRC = new int[(totalBitsComCRC+31)/32];

    // Copia os dados originais para o novo array
    System.arraycopy(quadro, 0, quadroComCRC, 0, quadro.length);

    // Registro do CRC. Comeca com tudo 1
    int crc = 0xFFFFFFFF;

    // Itera byte a byte
    for (int i=0; i<quadro.length; i++) {
      for (int j=3; j>=0; j--) {
        int umByte = (quadro[i] >> (j*8)) &0xFF;
        int indice = (crc ^ umByte) & 0xFF;
        crc = (crc >>> 8) ^ TabelaCRC.getCrc32Tabela()[indice];
      } // fim do for
    } // fim do for
    
    crc = crc ^ 0xFFFFFFFF;
    quadroComCRC[quadro.length] = crc;
    //usar polinomio CRC-32(IEEE 802)
    return quadroComCRC;
  }//fim do metodo camadaEnlaceDadosTransmissoraControleDeErroCRC

  /* ***************************************************************
  * Metodo: camadaEnlaceDadosTransmissoraControleDeErroCodigoDeHamming
  * Funcao: implementa o codigo de hamming para verificacao de erros
  * Parametros: quadro = unidade de transmissao que subdivide a mensagem
  * Retorno: int[] = bits apos inserir a informacao de controle
  *************************************************************** */
  public int[] camadaEnlaceDadosTransmissoraControleDeErroCodigoDeHamming(int[] quadro) {
    // Quantidade total de bits no quadro
    int quantidadeTotalDeBits = quadro.length*32;
    // Quantidade de bits de verificacao
    int r = 1; 
    
    // Iteracao para descobrir a quantidade de bits de verificacao necessaria
    while (!((quantidadeTotalDeBits+r+1) <= (1 << r))) {
      r++;
    } // fim do while

    // Quantidade total de bits no quadro com os bits de verificacao
    int quantidadeTotalDeBitsPosControle = quantidadeTotalDeBits+r;
    // Tamanho do array do novo quadro controlado
    int tamanhoArray = (quantidadeTotalDeBitsPosControle+31)/32;
    // Array que amazenara o novo quadro
    int[] quadroComHamming = new int[tamanhoArray];
    // Ponteiros para saber que indice do array usar
    int indiceEntrada = 0; 
    int indiceSaida = 0;
    int contadorBitSaida = 0;

    // Iteracao que ira transferir os bits do quadro para o novo quadro deixando espaco para os bits de verificacao
    for (int contadorBit=0; contadorBit<quantidadeTotalDeBits; contadorBit++) {
      
      // Se acabar de ler o ultimo bit de indice entrada, avanca
      if (contadorBit%32==0 & contadorBit!=0) {
        indiceEntrada++;
      } // fim do if
      
      // Le o bit do quadro
      int bit = (quadro[indiceEntrada] >>> 31-(contadorBit%32)) & 1;
      contadorBitSaida++;

      // Pula o bit de verificacao (bit potencia de 2)
      if (((contadorBitSaida+1) & (contadorBitSaida)) == 0) {
        contadorBitSaida++;
      } // fim do if
      
      // Se acabar de ler o ultimo bit de indice de saida, avanca
      if (contadorBitSaida%32==0 & contadorBitSaida!=0) {
        indiceSaida++;
      } // fim do if

      // Transfere o bit do quadro original para o quadro controlado
      quadroComHamming[indiceSaida] |= bit << 31-(contadorBitSaida%32);
    } // fim do for
    
    // Iteracao para inserir os bits de verificacao
    for (int i = 0; i < r; i++) {
      int posParidade = 1 << i;
      int paridade = 0;
      // Iteracao para ler todos os bits do quadro
      for (int j = 1; j <= quantidadeTotalDeBitsPosControle; j++) {
        // Verifica se o bit faz parte do controle de 'i' (bit de verificacao)
        if ((j & posParidade) != 0) {
          // Ponteiros de leitura
          int indiceArrayLeitura = (j - 1) / 32;
          int posBitLeitura = 31 - ((j - 1) % 32);
          // Le o bit
          int bit = (quadroComHamming[indiceArrayLeitura] >> posBitLeitura) & 1;
          // Calcula a paridade
          paridade = paridade ^ bit;
        } // fim do if
      } // fim do for
      // Se a paridade for 1 ele escreve
      if (paridade == 1) {
        // Ponteiro de escrita do quadro controlado
        int indiceArrayEscrita = (posParidade - 1) / 32;
        int posBitEscrita = 31 - ((posParidade - 1) % 32);
        // Escreve 1 no bit
        quadroComHamming[indiceArrayEscrita] |= (1 << posBitEscrita);
      } // fim do if
      // Se a paridade for 0 ele apenas avanca uma vez que ja esta escrito 0
    } // fim do for
    return quadroComHamming;
  } // fim do metodo camadaEnlaceDadosTransmissoraControleDeErroCodigoDeHamming

  /* ***************************************************************
  * Metodo: enviarEIniciarTimer
  * Funcao: envia o quadro para a proxima camada e inicializa o temporizador
  * Parametros: quadro = unidade de transmissao que subdivide a mensagem
  * Retorno: void
  *************************************************************** */
  private void enviarEIniciarTimer(int[] quadro) {
    System.out.println("Transmissor: Enviando quadro e iniciando timer de " + TIMEOUT_MS + "ms");
    camadaFisicaTransmissora.camadaFisicaTransmissora(quadro);
    
    // Agenda a tarefa de timeout
    timerHandle = scheduler.schedule(() -> {
      handleTimeout(quadro); // O que fazer se o tempo estourar
    }, TIMEOUT_MS, TimeUnit.MILLISECONDS);
  } // fim do metodo enviarEIniciarTimer

  /* ***************************************************************
  * Metodo: handleTimeout
  * Funcao: Retransmite o quadro e realiza o tempo de espera 
  * Parametros: quadroParaReenviar = quadro que deu erro e precisa ser reenviado
  * Retorno: void
  *************************************************************** */
  private void handleTimeout(int[] quadroParaReenviar) {
    synchronized (lock) {
      if (!esperandoAck) {
        // O ACK chegou exatamente no ultimo segundo. O timer e invalido.
        return; 
      } // fim do if

      System.out.println("Transmissor: TIMEOUT! Retransmitindo quadro...");
      // Re-envia e reinicia o timer.
      enviarEIniciarTimer(quadroParaReenviar);
    } // fim do ssynchronized
  } // fim do metodo handleTimeout

  /* ***************************************************************
  * Metodo: receberAck
  * Funcao: receber o ack e encerrar o temporizador
  * Parametros: 
  * Retorno: void
  *************************************************************** */
  public void receberAck() {
    synchronized (lock) {
      if (esperandoAck) {
        System.out.println("Transmissor: ACK Recebido!");
        esperandoAck = false; // Para de esperar
        if (timerHandle != null) {
          timerHandle.cancel(false); // Cancela o timer de timeout
        } // fim do if
        lock.notify(); // Acorda a thread da aplicacao (que esta no wait())
      } // fim do if
    } // fim do synchronized
  } // fim do metodo receberAck

  /* ***************************************************************
  * Metodo: enviarAck
  * Funcao: envia o ack para a proxima camada
  * Parametros: 
  * Retorno: void
  *************************************************************** */
  public void enviarAck(int[] quadro) {
    int[] quadroAck = new int[1];
    quadroAck[0] = 0x80000000;
    quadroAck[0] |= quadro[0] & 0xFF000000;

    // faz o controle de erro no ack
    quadroAck = camadaEnlaceDadosTransmissoraEnquadramento(quadroAck);
    quadroAck = camadaEnlaceDadosTransmissoraControleDeErro(quadroAck);
    camadaFisicaTransmissora.camadaFisicaTransmissora(quadroAck);
  } // fim do metodo enviarAck 

  /* ***************************************************************
  * Metodo: camadaEnlaceDadosTransmissoraJanelaDeslizanteUmBit
  * Funcao: realiza o controle de fluxo pelo metodo da janela deslizante de um bit
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: int[]
  *************************************************************** */
  public int[] camadaEnlaceDadosTransmissoraJanelaDeslizanteUmBit (int quadro []) {
    //implementacao do algoritmo
    return quadro;
  }//fim do metodo camadaEnlaceDadosTransmissoraJanelaDeslizanteUmBit
  
  /* ***************************************************************
  * Metodo: camadaEnlaceDadosTransmissoraJanelaDeslizanteGoBackN
  * Funcao: realiza o controle de fluxo pelo metodo da janela deslizante go and back n
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: int[]
  *************************************************************** */
  public int[] camadaEnlaceDadosTransmissoraJanelaDeslizanteGoBackN (int quadro []) {
    //implementacao do algoritmo
    return quadro;
  }//fim do metodo camadaEnlaceDadosTransmissoraJanelaDeslizanteGoBackN
  
  /* ***************************************************************
  * Metodo: camadaEnlaceDadosTransmissoraJanelaDeslizanteComRetransmissaoSeletiva
  * Funcao: realiza o controle de fluxo pelo metodo da janela com retransmissao seletiva
  * Parametros: quadro = conjunto de bits da mensagem
  * Retorno: int[]
  *************************************************************** */
  public int[] camadaEnlaceDadosTransmissoraJanelaDeslizanteComRetransmissaoSeletiva (int quadro []) {
    //implementacao do algoritmo
    return quadro;
  }//fim do metodo camadaEnlaceDadosTransmissoraJanelaDeslizanteComRetransmissaoSeletiva

  public int[] inserirNumeroDeSequencia(int[] quadro) {
    numDeSequencia=(numDeSequencia+1)%8;
    
    final int QUANTIDADE_BIT_ESCRITA = 8; // 1 (ACK/DADO) + 7 (Numero de sequencia)
    final int QUANTIDADE_TOTAL_BITS = (quadro[0] == 0 ? 0 : quadro.length*32); // Se for um pacote vazio (ACK antigo?), nao le
    final int NOVA_QUANTIDADE_TOTAL_BITS = QUANTIDADE_TOTAL_BITS+QUANTIDADE_BIT_ESCRITA;

    int[] quadroComSequencia = new int[(NOVA_QUANTIDADE_TOTAL_BITS+31)/32];

    // --- O NOVO PROTOCOLO ---
    // Bit 31: 0 = DADOS
    // Bit 30-24: Numero de Sequencia (0-7)
    // (O bit 31 ja e 0, entao so setamos o NS)
    quadroComSequencia[0] |= numDeSequencia << 24;
    // --- FIM DO PROTOCOLO ---

    int bitDeSaida = QUANTIDADE_BIT_ESCRITA; // Comeca a escrever DEPOIS dos 5 bits

    for (int i = 0; i < QUANTIDADE_TOTAL_BITS; i++) {
      // ... (o resto do seu loop de copia esta CORRETO) ...
      int indiceArrayEntrada = i / 32;
      int bitPosEntrada = 31 - (i % 32);
      int bit = (quadro[indiceArrayEntrada] >> bitPosEntrada) & 1;
      if (bit == 1) {
        int indiceArray = bitDeSaida / 32;
        int bitPos = 31 - (bitDeSaida % 32);
        quadroComSequencia[indiceArray] |= (1 << bitPos);
      }
      bitDeSaida++;
    }
    return quadroComSequencia;
  }
}