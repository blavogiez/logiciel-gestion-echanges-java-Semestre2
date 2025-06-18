\LoadClass[a4paper,12pt]{article} % Classe du document

%--------------------- Packages ------------------------

\RequirePackage[french]{babel} %Langue du document
\RequirePackage[utf8]{inputenc} %Caractères spéciaux
\RequirePackage[T1]{fontenc} %Quelques lettres qui sont pas inclus dans UTF-8
\RequirePackage{mathtools} %Paquet pour des équations et symboles mathématiques
\RequirePackage{siunitx} %Pour écrire avec la notation scientifique (Ex.: \num{2e+9})
\RequirePackage{float} %Pour placement d'images
\RequirePackage{graphicx} %Paquet pour insérer des images
\RequirePackage[justification=centering]{caption} %Pour les légendes centralisées
\RequirePackage{subcaption}
\RequirePackage{wallpaper}
\RequirePackage{nomencl}
\RequirePackage{minted}
\RequirePackage{xcolor}
\RequirePackage{listings}
\RequirePackage{tcolorbox}
\RequirePackage{enumitem}
\RequirePackage{tikz}
\RequirePackage{fontspec} % Pour utiliser des polices personnalisées

%\makenomenclature
\RequirePackage{fancyhdr}
%\pagestyle{fancy}
%\fancyheadoffset{1cm}
%\setlength{\headheight}{2cm}
\RequirePackage{url}
\RequirePackage[hidelinks]{hyperref}%Paquet pour insérer légendes dans des sous-figures comme Figure 1a, 1b
\RequirePackage[left=2.5cm,right=2.5cm,top=2cm,bottom=3.5cm]{geometry} %Configuration de la page

%-------------------- Informations sur le rapport ----------------------

\newcommand{\UE}[1]{\renewcommand{\UE}{#1}}
\newcommand{\sujet}[1]{\renewcommand{\sujet}{#1}}
\newcommand{\titre}[1]{\renewcommand{\titre}{#1}}
\newcommand{\enseignant}[1]{\renewcommand{\enseignant}{#1}}
\newcommand{\eleves}[1]{\renewcommand{\eleves}{#1}}

\newcommand{\fairemarges}{
\makenomenclature
\pagestyle{fancy}
\fancyheadoffset{1cm}
\setlength{\headheight}{2cm}
\lhead{\includegraphics[scale=0.1]{logos/logo_ECL.jpg}} %Affichage de l'image au top de la page
\rhead{\nouppercase{\leftmark}}
\rfoot{\thepage}
\cfoot{Rapport - \textbf{\titre}}
\lfoot{\UE}
}

\newcommand{\fairepagedegarde}{
\begin{titlepage}
 % date complète avec jour

\ThisLRCornerWallPaper{0.4}{logos/Stein6.jpg}
	\centering %Centraliser le contenu
	\includegraphics[width=0.8\textwidth]{logos/logo.png}\par\vspace{1cm} %Insertion du logo
	{\scshape\LARGE \textcolor{blue}{Université de Lille} \par} %Nom de l'université
	\vspace{1.5cm}%Espace de 1,5cm
	{\scshape\Large \UE \\ \sujet \\ \vspace{0.3cm}  Rapport\par} %sous-titre
	\vspace{1cm}%Espace de 1cm
    \rule{\linewidth}{0.2 mm} \\[0.4 cm]
	{\huge\bfseries \textcolor{blue}{\titre} \par} \
    \rule{\linewidth}{0.2 mm} \\[1.5 cm]
	\vspace{1cm}%Espace de 3cm

	\begin{minipage}{0.5\textwidth} %Minipage pour faire deux colonnes
		\begin{flushleft} \large %Envoyer à gauche
		\emph{\textbf{Élèves :}}\\ %Pour le titre au dessus des noms à gauche
        \eleves\\ %Remplacer pour chacun
		\end{flushleft}
	\end{minipage}
	~
	\begin{minipage}{0.4\textwidth}
		\begin{flushright} \large
		\emph{\textbf{Enseignant :}} \\
		 \enseignant \\
		\end{flushright}
	\end{minipage}\\[4cm]

	\vfill
	{\large \today\par}
	\end{titlepage}
}

\newcommand{\tabledematieres}{
\tableofcontents
\newpage
}

\newcommand{\insererfigure}[4]{
\begin{figure}[H]
\centering
\includegraphics[height=#2]{#1}
\caption{#3}
\label{fig: #4}
\end{figure}
}

\newcommand{\stylishcircle}{%
  \tikz[baseline=-.6ex] {
    \shade[ball color=gray!30, draw=gray!70]
      (0,0) circle (0.8ex);
  }%
}

\newcommand{\merci}{
  \vspace*{\fill}
  \begin{center}
    {\bfseries\LARGE Merci d'avoir consulté cette présentation}
  \end{center}
  \vspace*{\fill}
}

\setlist[itemize]{
  label=\stylishcircle,
  labelsep=1.5em,
  left=4em,              % indentation
  itemsep=0.5\baselineskip,   % espace entre les items
  parsep=0pt,            % pas d'espace supplémentaire entre paragraphes internes
  topsep=0.5\baselineskip % espace avant et après la liste
}

\makeatletter
\newcommand{\tsec}[1]{%
  \par\vspace{1\baselineskip}%
  \noindent
  {\normalsize\bfseries #1\par}%
  \vspace{1\baselineskip}%
}
\makeatother

% saut de page à chaque section

\let\oldsection\section
\renewcommand\section{%
  \clearpage% ou \newpage
  \oldsection}
