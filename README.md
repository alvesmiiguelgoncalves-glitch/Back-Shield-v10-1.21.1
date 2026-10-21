# Back Tool Tweak

Mod **client** (Fabric 1.21.1) que ajusta, **item por item**, a posicao, rotacao e escala dos itens que o **BackSlot** (e o BackSlot Addon) desenham nas costas e na cintura. Feito para consertar as armas grandes do Antarchy.

## O que faz
- Para cada item (ex.: `antarchy:battle_axe`): `x`, `y`, `z` (pixels, 1/16 de bloco), `rotX`, `rotY`, `rotZ` (graus), `scale` (0 = normal, 0.5 = 50% maior), `visible` e `mirror`.
- Os valores sao aplicados por cima da pose que o BackSlot ja calcula (nao mexe no BackSlot nem no Antarchy).
- Itens que nao estao na lista continuam como sempre.

## Config
- **Mod Menu** (opcional): botao de configurar abre uma aba por item.
- Arquivo: `config/backtooltweak.json` (criado na primeira execucao com os itens do Antarchy, tudo zerado).
- Comandos (ajuste ao vivo, olhando o personagem; use F5 para ver as costas):

```
/backtool list
/backtool add [item]                      (sem argumento = item da mao principal)
/backtool remove <item>
/backtool show <item>
/backtool set <item> <campo> <valor>      ex.: /backtool set antarchy:battle_axe rotX 90
/backtool nudge <item> <campo> <delta>    ex.: /backtool nudge antarchy:battle_axe y -2
/backtool reset <item>
/backtool reload                          (depois de editar o arquivo)
```
Campos: `x y z rotX rotY rotZ scale visible mirror` (visible/mirror: 0 ou 1).

## Dependencias
Fabric Loader >= 0.16.5, Fabric API, Cloth Config >= 15.0.130, **BackSlot**. Opcionais: Mod Menu, BackSlot Addon.

## Compilar
```
./gradlew build
```
O jar sai em `build/libs/backtooltweak-1.0.0.jar` (use o que NAO tem `-sources`). Para usar junto com `backshield/` e `totemslot/`, coloque esta pasta no repositorio, copie o `build.gradle` de uma delas e inclua a pasta no `settings.gradle` da raiz.

## Limitacoes / nao testado
- **Escrito sem poder compilar nem rodar o jogo.** Pode haver erro pontual de compilacao (nome de classe/metodo do Yarn) ou de mixin.
- Os mixins usam nomes **intermediary** (`method_3233`, `class_742`...) porque miram classes do BackSlot e o `HeldItemRenderer`. Funciona no jogo normal (producao). **Nao rode `runClient` em ambiente de desenvolvimento.**
- Alguns modelos do Antarchy (`big_bertha`, `royal_guardian_sword`, `royal_assailant_battleaxe`) trazem no proprio JSON um deslocamento `head` de +45,25 e escala 2. Ele continua sendo aplicado depois do seu ajuste (e e multiplicado pela sua escala). Se a arma ficar muito alta, compense com `y` negativo.
- Os sinais dos eixos dependem da pose do BackSlot. Se um eixo parecer invertido, use valor negativo ou `mirror`.
