# aEventos

O ``aEventos`` é um plugin para servidores de Minecraft que traz vários eventos automáticos de forma fácil e rápida. O intuito desse fork é continuar atualizando o projeto, já que ele não recebe mais atualizações desde 2022 e o projeto original algumas coisas deixaram de funcionar como eventos e GUIS.

Infelizmente devido algumas integrações que "sumiram" da comunidade, algums hooks tiveram que ser removidos, como compatibilidade com yClans.

Caso você seja desenvolvedor e queira contribuir com o projeto, sinta-se livre para fazer um fork e enviar um pull request e ajude esse projeto incrível a continuar vivo.

## Eventos:
O plugin conta atualmente com ``25`` eventos no total, sendo ``18`` presenciais:
* Sign (Você ganha o evento ao clicar na placa. Com uma configuração, também pode ser usado para Parkour.)
* Campo Minado
* Spleef
* Semáforo
* Batata Quente
* Frog
* Fight
* Killer
* Sumo
* Fall (Caso você ainda esteja vivo depois de X segundos do início do evento, você ganha. Pode ser usado para fazer um evento Astronauta, por exemplo.)
* Paintball
* Hunter (Paintball com um sistema de pontos.)
* Quiz
* Anvil
* Guerra (Gladiador)
* Nexus
* Thor
* Battle Royale

e ``7`` no chat:
* Votação
* Loteria
* Bolão
* Matemática
* Palavra
* FastClick
* Sorteio

## Comandos:
|Comando         |Descrição                      |Permissão                    |
|----------------|-------------------------------|-----------------------------|
|/evento        | Comando usado para entrar no evento. Caso não esteja acontecendo um no momento, mostrará a lista de comandos.|A permissão depende do evento. `aEventos.admin` para mostrar os comandos de administrador.           |
|/evento sair    |Sai do evento atual.|Nenhuma.       |
|/evento (camarote/assistir) |Assista o evento atual, caso o mesmo permita.	     |Além da permissão necessária para participar do evento, `aEventos.spectator`			   |
|/evento iniciar [evento]|Inicia o evento especificado, caso não esteja acontecendo outro no momento.|`aEventos.admin`			   |
|/evento (cancelar/parar)|Cancela o evento atual.|`aEventos.admin`	
|/evento forcestart|Força a inicialização do evento atual, mesmo que as chamadas não tenham acabado.|`aEventos.admin`
|/evento kick [jogador] |Expulsa o jogador do evento.|`aEventos.admin`
|/evento setup [evento] |Inicia a configuração do evento especificado.|`aEventos.admin`			   |
|/evento setup sair|Saia da configuração do evento especificado.|`aEventos.admin`			   |
|/evento criarconfig [evento] |Cria um arquivo de configuração de exemplo do evento.|`aEventos.admin`			   |
|/evento reload|Recarrega as configurações do plugin.	     |`aEventos.admin`			   |
|/evento (ajuda/help) | Mostra a lista de comandos. |Nenhuma. `aEventos.admin` para mostrar os comandos de administrador.           |



## Configurações:

Quando você carregar o plugin pela primeira vez, serão criadas ``22`` configurações de exemplo na pasta ``eventos`` com todos os eventos do plugin. Cada tipo de evento possui suas configurações únicas, mas neste exemplo será configurado o arquivo ``parkour.yml``.

```yml
Evento:
  # Configurações do evento.
  Title: "Parkour" # O nome do evento. Será usado na placeholder @name.
  Type: "sign" # O tipo do evento. Não modifique!
  Calls: 5 # Quantas chamadas o evento terá.
  Calls interval: 15 # O intervalo (em segundos) entre as chamadas.
  Mininum players: 2 # O mínimo de jogadores para iniciar o evento.
  Spectator mode: true # Este evento possui camarote?
  Empty inventory: false # Esse evento requer que você esteja com o inventário vazio?
  Permission: "aEventos.evento" # A permissão necessária para participar do evento.
  Count participation: true # Esse evento contará participação?
  Count victory: true # Esse evento contará vitória?
  Return on damage: true # Caso esteja ativado, o jogador voltará a entrada ou ao checkpoint ao tomar dano de queda.

# Mensagens
Messages:
  Broadcast:
    - ""
    - "&3[@name] &bIniciando evento &f@name"
    - "&3[@name] &bPara participar, digite &f/evento"
    - "&3[@name] &bPrêmio: &2$&f10.485,96 &b+ &6[Parkour]"
    - "&3[@name] &bJogadores no evento: &f@players"
    - "&3[@name] &bChamadas restantes: &f@broadcasts"
    - ""
  Start:
    - ""
    - "&3[@name] &bIniciando evento..."
    - ""
  Winner:
    - ""
    - "&3[@name] &bO evento &f@name &bfoi finalizado!"
    - "&3[@name] &bO vencedor foi &f@winner&b!"
    - ""
  No winner:
    - ""
    - "&3[@name] &bO evento &f@name &bfoi finalizado!"
    - "&3[@name] &bNão houveram vencedores."
    - ""
  No players:
    - ""
    - "&3[@name] &bEvento cancelado."
    - "&3[@name] &bA quantidade mínima de jogadores não foi atingida."
    - ""
  Cancelled:
    - ""
    - "&3[@name] &bO evento foi cancelado manualmente."
    - ""
  Sign: # Estilo da placa de vitória. Você terá que trocar-la toda vez que fizer uma modificação aqui.
    - "&b&l[Parkour]"
    - "&3Clique aqui"
    - "&3para ganhar o"
    - "&3evento."
  Checkpoint: # Estilo da placa de checkpoint. Você terá que trocar-la toda vez que fizer uma modificação aqui.
    - "&a&l[Checkpoint]"
    - "&2Clique aqui"
    - "&2para salvar o"
    - "&2checkpoint."
  Checkpoint saved: "&3[@name] &bCheckpoint salvo com sucesso!"
  Checkpoint back: "&3[@name] &bVocê voltou ao checkpoint."
  Back: "&3[@name] &bVocê voltou ao início."

# Recompensas. Elas serão dadas ao vencedor.
Rewards:
  Tag: # Tag do LegendChat.
    Enabled: true # Ativar a tag do evento?
    Name: "aEventos_parkour" # O nome da tag. Você a colocará na configuração do LegendChat. Nesse caso, {aEventos_parkour}
    Style: "&6[Parkour] " # A aparência da tag.
  Commands: # Lista de comandos que serão executados no vencedor.
    - "give @winner diamond 1"

# Localizações. Você configurará essa seção usando o comando /evento setup parkour
Locations:
  Lobby: []
  Entrance: []
  Spectator: []
  Exit: []

```

Após fazer as mudanças, basta entrar no jogo e digitar o comando ``/evento setup parkour``. Note que o argumento parkour se refere ao nome do arquivo de configuração. Ao digitar o comando, você receberá no chat a lista de comandos para definir as posições.

Para colocar as placas do evento, digite na primeira linha ``[evento]``. Caso queira colocar uma placa de vitória, digite ``vitoria`` na segunda linha. Caso queira uma placa de checkpoint, digite ``checkpoint``. 

Caso o evento seja de eliminação (Campo Minado, Spleef, Frog, Sumo), o jogador será eliminado ao tocar na água.

Depois de definir-las, digite ``/evento setup sair`` para salvar as alterações. Após colocar as placas, teste o evento o iniciando com o comando ``/evento iniciar parkour``. 

Caso queira criar um novo evento desse tipo, basta copiar o arquivo de configuração com um outro nome e repitir esse processo.

## Placeholders:
Aqui estão as placeholders disponíveis:

- %aEventos_wins_total% - Mostra o total de vitórias do jogador.
- %aEventos_participations_total% - Mostra o total de participações do jogador.
- %aEventos_wins_[NOME DO ARQUIVO DE CONFIGURAÇÃO]% - Mostra o total de vitórias que o jogador teve no evento específico.
- %aEventos_participations_[NOME DO ARQUIVO DE CONFIGURAÇÃO]% - Mostra o total de participações que o jogador teve no evento específico.
- %aEventos_tag_[NOME DA TAG]% - Mostra a tag do evento, caso o jogador á possua.<br>Substitua o "[NOME DA TAG]" pelo nome que está na configuração ``Rewards.Tag.Name``.

## Suporte para RGB:

Aqui você pode usar cores RGB em suas mensagens. Para isso, use as seguintes tags:

- Para usar uma cor sólida, use ``<SOLID:FF0080>Colorido!``
- Para fazer um gradiente, use ``<GRADIENT:2C08BA>Gradiente!</GRADIENT:028A97>``
- Para fazer um texto colorido, use ``<RAINBOW1>Arco-íris!</RAINBOW>``. O número "1" depois da primeira tag representa a saturação.


## Créditos:

- [Repositório original do Ars3ne](https://github.com/Ars3ne/aEventos)
- [tristiisch74](https://www.spigotmc.org/members/tristiisch74.149406/) por criar a base da classe [Cuboid](https://www.spigotmc.org/threads/region-cuboid.329859/).
- [tchristofferson](https://github.com/tchristofferson) por criar a classe [ConfigUpdater](https://github.com/tchristofferson/Config-Updater).