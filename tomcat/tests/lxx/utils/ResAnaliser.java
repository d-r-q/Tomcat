/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ResAnaliser {

    private static final String str1 = "| [[lxx.Tomcat]] 3.44mt1 || [[Author]] || Algorithm ||50,33 ||58,78 ||60,74 ||53,65 ||61,39 ||66,73 ||50,85 ||58,10 ||70,98 ||56,75 ||64,31 ||64,29 ||60,67 ||66,44 ||69,22 ||62,22 ||67,78 ||62,73 ||66,16 ||67,02 ||62,27 ||61,50 ||63,77 ||68,03 ||61,95 ||63,17 ||58,44 ||68,69 ||61,93 ||61,04 ||61,11 ||55,80 ||69,70 ||61,91 ||75,78 ||63,31 ||72,82 ||58,74 ||64,72 ||70,47 ||69,90 ||63,81 ||64,66 ||54,83 ||70,40 ||64,07 ||63,49 ||74,84 ||59,13 ||69,86 ||62,57 ||67,37 ||67,02 ||64,93 ||65,59 ||64,37 ||68,11 ||70,27 ||64,30 ||75,87 ||69,87 ||76,24 ||64,87 ||61,94 ||69,73 ||69,35 ||73,23 ||80,29 ||61,75 ||61,74 ||65,56 ||57,94 ||68,31 ||68,61 ||72,69 ||64,28 ||67,65 ||69,78 ||73,08 ||71,51 ||76,86 ||75,08 ||79,06 ||80,25 ||79,18 ||81,88 ||75,82 ||75,34 ||70,44 ||75,98 ||70,31 ||68,37 ||67,31 ||62,11 ||74,47 ||82,73 ||68,61 ||66,16 ||73,32 ||57,41 ||72,06 ||82,54 ||86,24 ||70,16 ||74,73 ||81,92 ||83,67 ||78,55 ||82,34 ||74,18 ||80,80 ||73,97 ||71,34 ||81,86 ||79,55 ||66,65 ||65,96 ||75,57 ||78,44 ||87,25 ||63,00 ||77,98 ||75,06 ||65,79 ||86,80 ||80,80 ||70,74 ||86,76 ||79,15 ||77,75 ||77,05 ||65,14 ||82,72 ||80,50 ||77,35 ||81,93 ||77,16 ||73,26 ||77,55 ||74,81 ||62,62 ||96,65 ||69,88 ||63,47 ||81,83 ||75,55 ||81,70 ||76,53 ||77,63 ||81,33 ||73,51 ||86,14 ||63,53 ||93,72 ||77,73 ||78,21 ||75,01 ||65,94 ||65,26 ||81,11 ||76,35 ||70,32 ||79,54 ||89,24 ||90,30 ||90,05 ||75,87 ||79,73 ||82,30 ||79,85 ||83,15 ||82,68 ||81,08 ||80,18 ||75,94 ||79,14 ||80,36 ||90,72 ||85,48 ||88,24 ||79,01 ||76,56 ||75,85 ||83,90 ||81,49 ||74,18 ||81,20 ||76,13 ||85,90 ||66,93 ||78,56 ||81,91 ||77,96 ||74,94 ||67,25 ||78,88 ||76,76 ||85,39 ||82,34 ||67,84 ||73,17 ||76,04 ||77,29 ||71,78 ||77,89 ||79,48 ||78,53 ||78,51 ||78,64 ||77,02 ||80,14 ||74,43 ||78,63 ||81,35 ||81,16 ||82,85 ||81,94 ||78,40 ||88,72 ||78,27 ||82,14 ||75,57 ||85,91 ||89,23 ||84,42 ||89,68 ||91,68 ||76,36 ||89,87 ||84,35 ||80,61 ||83,55 ||88,06 ||70,52 ||77,09 ||77,87 ||73,59 ||98,74 ||85,41 ||89,51 ||78,37 ||81,81 ||74,55 ||69,39 ||82,34 ||81,09 ||80,57 ||84,00 ||80,67 ||82,92 ||80,50 ||78,22 ||75,75 ||77,42 ||86,20 ||81,86 ||81,61 ||80,99 ||79,33 ||71,11 ||81,75 ||83,17 ||83,09 ||82,85 ||99,36 ||89,27 ||80,82 ||77,25 ||85,49 ||78,11 ||96,89 ||80,41 ||83,74 ||77,85 ||77,20 ||77,99 ||83,81 ||82,76 ||88,15 ||98,44 ||85,75 ||80,00 ||83,87 ||73,52 ||84,00 ||93,41 ||92,04 ||82,59 ||79,91 ||81,92 ||85,18 ||81,45 ||80,42 ||76,67 ||80,18 ||80,93 ||75,59 ||86,31 ||90,80 ||90,49 ||79,14 ||81,57 ||84,18 ||79,70 ||80,55 ||86,36 ||75,13 ||83,70 ||75,96 ||78,15 ||89,81 ||81,09 ||89,04 ||78,72 ||85,93 ||83,35 ||78,73 ||82,13 ||81,30 ||90,40 ||75,49 ||79,01 ||81,69 ||78,55 ||84,92 ||77,16 ||89,36 ||87,94 ||85,33 ||78,21 ||97,99 ||76,39 ||81,29 ||84,44 ||84,98 ||87,13 ||71,80 ||80,14 ||90,09 ||80,72 ||86,65 ||82,04 ||72,93 ||85,86 ||92,57 ||87,12 ||77,83 ||89,99 ||70,01 ||78,33 ||64,62 ||87,15 ||87,82 ||82,72 ||86,14 ||91,21 ||79,76 ||81,89 ||81,47 ||88,64 ||88,87 ||84,39 ||86,55 ||76,67 ||71,53 ||86,29 ||82,52 ||81,03 ||92,02 ||86,18 ||90,17 ||69,74 ||79,25 ||81,81 ||77,63 ||98,47 ||82,88 ||81,49 ||86,23 ||88,36 ||90,29 ||88,38 ||90,14 ||79,25 ||83,56 ||92,58 ||89,86 ||86,15 ||87,97 ||95,57 ||85,03 ||96,76 ||85,50 ||83,35 ||96,57 ||93,61 ||93,72 ||89,88 ||91,98 ||83,89 ||87,38 ||90,19 ||84,81 ||90,20 ||85,67 ||95,77 ||88,22 ||75,01 ||89,14 ||98,52 ||86,83 ||77,32 ||72,31 ||84,87 ||91,63 ||94,02 ||86,14 ||85,01 ||84,62 ||99,20 ||93,04 ||82,20 ||87,71 ||90,35 ||98,91 ||87,25 ||87,42 ||90,78 ||75,94 ||93,24 ||93,21 ||85,07 ||96,26 ||95,50 ||99,22 ||93,06 ||87,74 ||89,76 ||89,29 ||86,43 ||96,20 ||77,68 ||91,06 ||99,61 ||91,47 ||86,94 ||96,62 ||75,12 ||91,97 ||98,52 ||91,35 ||84,97 ||79,86 ||86,95 ||98,02 ||91,84 ||95,44 ||98,83 ||90,20 ||90,00 ||97,91 ||86,93 ||90,67 ||83,28 ||91,71 ||87,47 ||95,30 ||94,79 ||96,95 ||97,28 ||84,78 ||92,75 ||98,57 ||81,21 ||97,56 ||93,31 ||78,47 ||91,22 ||96,44 ||90,10 ||89,82 ||89,10 ||82,59 ||94,25 ||85,89 ||98,24 ||91,70 ||99,98 ||95,97 ||84,65 ||95,40 ||92,72 ||89,36 ||98,26 ||98,28 ||96,81 ||98,87 ||99,72 ||90,36 ||89,97 ||92,62 ||91,44 ||94,44 ||84,88 ||98,40 ||97,44 ||98,65 ||99,41 ||96,50 ||99,55 ||93,93 ||89,37 ||99,37 ||95,45 ||88,18 ||97,99 ||96,67 ||99,32 ||95,60 ||85,60 ||89,98 ||86,26 ||93,09 ||83,85 ||96,16 ||82,89 ||79,12 ||97,95 ||96,87 ||85,91 ||88,80 ||84,78 ||91,91 ||97,07 ||96,69 ||96,84 ||92,07 ||86,58 ||98,75 ||96,36 ||88,82 ||98,10 ||99,53 ||95,88 ||87,87 ||94,79 ||99,06 ||97,23 ||98,51 ||98,76 ||95,99 ||97,95 ||87,18 ||95,10 ||90,42 ||96,01 ||87,61 ||92,33 ||99,02 ||90,12 ||88,31 ||93,93 ||98,03 ||95,63 ||99,83 ||84,64 ||99,13 ||95,56 ||91,52 ||97,06 ||97,07 ||96,24 ||94,53 ||98,95 ||96,17 ||92,57 ||94,21 ||88,89 ||97,15 ||89,69 ||99,59 ||98,37 ||91,03 ||91,50 ||89,78 ||99,56 ||95,83 ||99,11 ||94,67 ||90,04 ||96,73 ||97,11 ||97,85 ||94,82 ||93,14 ||99,81 ||97,84 ||90,69 ||97,08 ||99,78 ||89,49 ||99,80 ||97,95 ||91,61 ||98,37 ||94,87 ||97,39 ||91,73 ||99,46 ||93,74 ||94,03 ||92,33 ||86,41 ||97,92 ||96,09 ||100,00 ||93,62 ||99,57 ||99,27 ||93,57 ||94,38 ||95,77 ||97,52 ||97,92 ||99,99 ||99,87 ||91,90 ||99,68 ||98,60 ||90,37 ||97,64 ||97,90 ||98,51 ||98,01 ||97,01 ||93,94 ||96,51 ||96,37 ||99,74 ||97,17 ||95,58 ||96,11 ||86,71 ||99,40 ||90,62 ||98,25 ||99,82 ||99,26 ||94,28 ||95,31 ||98,85 ||93,06 ||95,88 ||99,50 ||99,42 ||96,36 ||99,77 ||93,49 ||97,58 ||97,84 ||97,45 ||95,66 ||96,72 ||99,01 ||91,11 ||94,30 ||98,97 ||94,38 ||96,91 ||99,30 ||99,70 ||93,63 ||95,56 ||92,74 ||99,66 ||98,29 ||99,88 ||91,61 ||98,79 ||90,18 ||92,54 ||91,49 ||98,31 ||96,14 ||98,02 ||96,88 ||96,67 ||93,15 ||99,12 ||99,28 ||91,76 ||99,87 ||99,77 ||96,17 ||87,25 ||99,38 ||99,12 ||86,43 ||98,60 ||99,60 ||96,27 ||98,11 ||99,13 ||91,10 ||97,43 ||99,44 ||98,96 ||99,93 ||93,73 ||88,15 ||97,37 ||96,25 ||93,03 ||99,99 ||98,58 ||95,64 ||99,79 ||94,71 ||92,81 ||93,12 ||93,74 ||98,40 ||86,34 ||99,07 ||98,15 ||98,72 ||95,13 ||96,74 ||95,98 ||99,00 ||98,77 ||99,63 ||99,40 ||84,26 ||99,89 ||95,39 ||98,57 ||99,67 ||98,34 ||98,76 ||95,85 ||98,60 ||99,85 ||99,65 ||96,45 ||98,01 ||99,32 ||98,70 ||84,33 ||98,13 ||97,09 ||98,66 ||97,31 ||98,85 ||99,16 ||94,13 ||99,37 ||98,24 ||99,48 ||95,64 ||97,47 ||99,29 ||99,47 ||99,99 ||99,24 ||99,30 ||96,53 ||99,77 ||99,44 ||99,97 ||99,99 ||97,49 ||99,84 ||95,86 ||97,02 ||96,97 ||94,13 ||99,75 ||99,93 ||99,62 ||99,88 ||99,29 ||99,93 ||99,94 ||99,81 ||98,79 ||94,49 ||99,91 ||99,06 ||99,11 ||100,00 ||94,77 ||84,50 ||100,00 ||100,00 ||98,96 ||97,52 ||99,76 ||99,99 ||99,91 ||99,16 ||99,80 ||99,76 ||99,13 ||100,00 ||100,00 ||99,95 ||99,99 ||99,98 ||99,93 ||98,65 ||100,00 ||99,98 ||99,99 ||99,99 ||99,90 ||99,66 ||100,00 ||100,00 ||99,96 ||99,02 ||99,96 ||98,72 ||99,98 ||99,99 ||99,78 ||100,00 ||99,95 ||98,67 ||100,00 ||100,00 ||99,97 ||100,00 ||100,00 ||100,00 ||100,00 ||'''86,71 (88,45)''' ||'''86,71 (88,45)''' ||4 seasons";
    private static final String str2 = "| [[lxx.Tomcat]] 3.45wsm || [[Author]] || Algorithm ||57,96 ||62,61 ||56,84 ||50,12 ||71,74 ||56,66 ||45,98 ||55,92 ||75,04 ||46,55 ||70,34 ||69,88 ||56,16 ||71,07 ||62,42 ||64,07 ||60,79 ||70,86 ||69,49 ||64,48 ||64,39 ||68,21 ||62,36 ||65,77 ||63,09 ||71,01 ||64,48 ||66,11 ||56,06 ||61,94 ||61,38 ||59,49 ||58,61 ||58,82 ||67,37 ||70,14 ||69,99 ||69,61 ||57,14 ||68,03 ||66,99 ||67,70 ||61,76 ||66,96 ||70,78 ||74,12 ||62,86 ||71,79 ||60,16 ||73,83 ||61,17 ||69,28 ||72,74 ||58,13 ||66,12 ||76,41 ||64,55 ||67,14 ||64,69 ||75,22 ||68,49 ||79,83 ||70,47 ||62,79 ||74,23 ||75,68 ||73,89 ||81,98 ||59,30 ||66,79 ||66,75 ||67,09 ||70,18 ||69,88 ||77,56 ||65,68 ||66,81 ||66,54 ||80,40 ||64,73 ||77,42 ||69,56 ||72,64 ||84,79 ||77,19 ||83,61 ||73,56 ||75,49 ||72,53 ||70,47 ||68,35 ||71,50 ||74,49 ||73,57 ||68,43 ||84,58 ||72,81 ||72,05 ||79,24 ||62,18 ||78,67 ||81,51 ||85,79 ||80,04 ||74,79 ||86,15 ||87,97 ||79,78 ||82,81 ||76,87 ||83,54 ||69,52 ||66,41 ||79,95 ||87,58 ||73,14 ||64,19 ||80,55 ||75,06 ||95,75 ||67,18 ||74,69 ||73,80 ||70,40 ||85,54 ||75,95 ||76,59 ||82,08 ||84,00 ||81,49 ||75,06 ||70,50 ||82,48 ||70,32 ||77,77 ||80,55 ||76,21 ||64,37 ||80,70 ||64,35 ||63,87 ||96,44 ||62,28 ||60,14 ||84,39 ||73,78 ||83,26 ||79,82 ||76,76 ||80,87 ||64,58 ||80,04 ||63,29 ||96,37 ||71,56 ||79,97 ||77,75 ||68,76 ||79,76 ||79,90 ||73,38 ||72,03 ||79,63 ||88,52 ||89,84 ||90,24 ||76,41 ||78,72 ||82,79 ||83,08 ||73,39 ||84,76 ||82,65 ||78,79 ||69,65 ||79,01 ||80,97 ||90,54 ||88,00 ||88,87 ||81,27 ||71,30 ||81,71 ||83,42 ||74,85 ||76,52 ||80,30 ||74,52 ||83,70 ||67,63 ||75,79 ||80,03 ||79,18 ||64,90 ||73,12 ||81,98 ||81,47 ||85,70 ||86,38 ||73,22 ||75,96 ||82,57 ||75,57 ||73,65 ||82,81 ||77,19 ||74,85 ||81,33 ||81,35 ||78,61 ||81,91 ||76,22 ||83,27 ||79,78 ||85,20 ||80,93 ||82,25 ||72,95 ||87,65 ||80,91 ||85,03 ||74,81 ||87,01 ||86,00 ||89,07 ||89,10 ||90,28 ||75,49 ||89,68 ||79,03 ||81,25 ||81,03 ||88,57 ||77,41 ||80,01 ||72,40 ||74,79 ||98,97 ||83,80 ||89,43 ||82,22 ||88,41 ||72,25 ||67,06 ||82,74 ||86,18 ||74,99 ||82,63 ||82,94 ||85,76 ||89,24 ||80,54 ||82,22 ||70,80 ||88,67 ||84,01 ||80,59 ||76,06 ||80,71 ||77,38 ||78,56 ||85,33 ||71,64 ||81,59 ||99,51 ||90,19 ||78,52 ||75,84 ||86,08 ||74,59 ||97,72 ||81,25 ||85,56 ||80,11 ||78,09 ||83,42 ||83,39 ||83,47 ||86,03 ||98,91 ||86,55 ||78,87 ||83,13 ||79,28 ||85,27 ||92,98 ||91,32 ||84,89 ||78,99 ||78,79 ||81,75 ||83,65 ||70,14 ||69,34 ||77,77 ||83,62 ||78,99 ||83,18 ||95,04 ||89,79 ||77,05 ||83,71 ||87,01 ||85,59 ||85,67 ||79,95 ||81,57 ||84,67 ||71,05 ||85,69 ||88,56 ||75,96 ||90,60 ||82,50 ||83,88 ||86,60 ||81,27 ||79,18 ||80,16 ||92,11 ||82,89 ||82,40 ||76,42 ||77,75 ||87,10 ||79,86 ||85,57 ||87,98 ||85,88 ||75,20 ||98,04 ||75,90 ||79,62 ||85,45 ||85,26 ||88,69 ||78,61 ||77,76 ||90,86 ||80,79 ||85,97 ||78,24 ||71,11 ||89,87 ||93,27 ||91,91 ||73,75 ||88,93 ||69,59 ||83,18 ||70,35 ||88,62 ||84,92 ||83,92 ||81,31 ||91,35 ||80,50 ||78,05 ||79,51 ||88,98 ||89,84 ||85,11 ||90,89 ||83,52 ||72,58 ||84,59 ||80,21 ||82,07 ||92,57 ||87,30 ||88,24 ||71,65 ||76,24 ||79,45 ||74,72 ||98,75 ||75,17 ||83,23 ||81,45 ||85,76 ||90,84 ||89,54 ||90,10 ||82,83 ||84,60 ||94,02 ||91,05 ||90,06 ||88,35 ||96,34 ||85,28 ||96,44 ||88,17 ||85,30 ||97,68 ||93,12 ||94,76 ||91,01 ||93,38 ||81,85 ||85,87 ||88,13 ||88,04 ||91,49 ||80,33 ||97,49 ||89,16 ||73,30 ||89,62 ||98,83 ||87,35 ||79,69 ||72,61 ||80,00 ||95,21 ||95,30 ||87,04 ||85,49 ||85,64 ||99,14 ||91,85 ||79,96 ||87,28 ||88,15 ||97,67 ||86,08 ||89,20 ||88,81 ||73,19 ||91,79 ||92,88 ||85,87 ||93,86 ||96,60 ||99,35 ||93,12 ||90,23 ||86,05 ||89,90 ||89,60 ||93,41 ||74,81 ||89,52 ||99,52 ||89,66 ||83,71 ||95,64 ||77,68 ||90,24 ||97,10 ||89,51 ||88,76 ||76,41 ||81,98 ||98,96 ||89,20 ||96,09 ||97,81 ||93,83 ||89,13 ||97,36 ||88,83 ||89,76 ||86,92 ||93,32 ||88,72 ||94,86 ||96,95 ||99,06 ||98,75 ||79,85 ||94,17 ||97,91 ||83,48 ||97,93 ||93,50 ||78,67 ||85,89 ||96,12 ||89,91 ||87,33 ||89,89 ||81,52 ||95,79 ||80,19 ||98,28 ||87,96 ||100,00 ||95,60 ||81,74 ||98,11 ||92,22 ||90,10 ||95,78 ||98,71 ||96,81 ||99,03 ||99,64 ||93,29 ||88,14 ||92,65 ||87,22 ||95,12 ||83,37 ||98,70 ||98,18 ||98,05 ||99,62 ||97,14 ||98,59 ||90,08 ||89,47 ||99,42 ||98,30 ||88,89 ||99,04 ||95,60 ||99,66 ||97,01 ||81,28 ||90,78 ||88,64 ||92,94 ||90,11 ||97,19 ||86,51 ||81,65 ||98,81 ||96,43 ||80,05 ||85,46 ||88,51 ||92,32 ||97,45 ||95,55 ||97,40 ||92,21 ||87,80 ||99,23 ||94,91 ||88,79 ||99,13 ||100,00 ||94,83 ||88,27 ||96,27 ||98,13 ||96,08 ||95,92 ||95,90 ||96,29 ||99,16 ||86,63 ||94,66 ||95,68 ||97,75 ||86,34 ||94,82 ||98,79 ||89,72 ||90,26 ||95,11 ||98,38 ||95,78 ||99,90 ||80,57 ||99,55 ||96,66 ||88,49 ||97,93 ||98,20 ||96,32 ||96,62 ||98,91 ||95,56 ||92,78 ||94,56 ||92,26 ||97,25 ||87,40 ||99,30 ||98,91 ||89,44 ||89,72 ||88,53 ||98,23 ||96,33 ||98,95 ||94,29 ||95,55 ||96,11 ||97,21 ||98,44 ||96,35 ||93,32 ||100,00 ||97,71 ||89,79 ||93,38 ||100,00 ||90,19 ||99,64 ||98,78 ||92,38 ||99,45 ||94,06 ||97,12 ||92,30 ||99,93 ||95,42 ||95,63 ||93,48 ||84,39 ||98,81 ||92,78 ||100,00 ||93,25 ||99,57 ||99,67 ||94,85 ||93,58 ||94,43 ||97,82 ||98,79 ||99,51 ||99,59 ||90,27 ||99,42 ||98,88 ||88,37 ||99,18 ||99,00 ||98,79 ||96,44 ||98,06 ||94,57 ||95,50 ||95,62 ||99,58 ||98,40 ||96,23 ||92,65 ||87,03 ||100,00 ||90,48 ||97,66 ||99,93 ||99,63 ||96,42 ||94,07 ||99,24 ||95,65 ||96,57 ||98,85 ||98,88 ||97,47 ||100,00 ||93,25 ||97,70 ||98,94 ||98,38 ||94,45 ||96,06 ||98,82 ||91,15 ||93,74 ||99,50 ||93,66 ||96,23 ||98,35 ||99,28 ||85,48 ||95,85 ||91,74 ||99,61 ||98,14 ||99,68 ||90,93 ||98,83 ||92,12 ||94,73 ||88,79 ||98,80 ||96,32 ||99,33 ||95,99 ||95,27 ||94,22 ||98,71 ||99,14 ||89,75 ||99,66 ||99,78 ||97,83 ||87,43 ||99,18 ||98,42 ||86,36 ||99,74 ||99,98 ||95,94 ||97,92 ||99,68 ||91,01 ||98,62 ||98,32 ||97,83 ||100,00 ||94,20 ||90,74 ||95,89 ||97,28 ||93,24 ||99,98 ||98,88 ||94,29 ||99,03 ||94,67 ||89,81 ||95,12 ||91,90 ||98,74 ||89,19 ||99,38 ||98,66 ||99,01 ||93,09 ||96,81 ||96,73 ||98,94 ||98,88 ||99,92 ||99,65 ||82,30 ||100,00 ||96,10 ||98,54 ||99,91 ||98,04 ||98,62 ||96,74 ||99,03 ||99,82 ||99,85 ||96,42 ||98,31 ||98,12 ||99,56 ||77,69 ||98,32 ||96,65 ||97,96 ||95,02 ||98,89 ||98,91 ||93,72 ||99,65 ||98,74 ||99,42 ||94,63 ||97,25 ||99,69 ||99,71 ||99,34 ||98,56 ||99,12 ||96,55 ||99,65 ||99,26 ||99,47 ||99,98 ||99,06 ||99,76 ||94,79 ||97,70 ||96,48 ||96,54 ||99,70 ||100,00 ||99,39 ||99,46 ||99,40 ||99,81 ||99,83 ||99,53 ||99,79 ||94,51 ||100,00 ||98,97 ||99,31 ||100,00 ||94,49 ||86,10 ||99,98 ||100,00 ||98,17 ||99,93 ||99,68 ||100,00 ||99,72 ||98,55 ||99,16 ||99,64 ||99,14 ||100,00 ||100,00 ||99,79 ||99,98 ||100,00 ||100,00 ||98,49 ||100,00 ||100,00 ||100,00 ||99,93 ||99,76 ||99,25 ||99,98 ||99,98 ||99,93 ||99,43 ||100,00 ||99,16 ||99,94 ||100,00 ||99,74 ||100,00 ||99,98 ||98,65 ||100,00 ||100,00 ||99,97 ||100,00 ||100,00 ||100,00 ||100,00 ||'''86,86 (88,85)''' ||'''86,86 (88,85)''' ||4 seasons";

    public static void main(String[] args) {
        String[] str1Arr = str1.split("\\|\\|");
        String[] str2Arr = str2.split("\\|\\|");
        AvgValue diff = new AvgValue(10000);
        AvgValue avgAps1 = new AvgValue(10000);
        AvgValue avgAps2 = new AvgValue(10000);
        AvgValue avgDelta1 = new AvgValue(10000);
        AvgValue avgDelta2 = new AvgValue(10000);
        double minDiff = Integer.MAX_VALUE;
        double maxDiff = Integer.MIN_VALUE;
        Double prevAps1 = null;
        Double prevAps2 = null;
        int cnt = 0;
        for (int i = 0; i < str1Arr.length; i++) {
            try {
                final double aps1 = new Double(str1Arr[i].trim().replace(",", "."));
                final double aps2 = new Double(str2Arr[i].trim().replace(",", "."));
                if (Double.isNaN(aps1) || Double.isNaN(aps2)) {
                    continue;
                }
                avgAps1.addValue(aps1);
                avgAps2.addValue(aps2);
                final double d = aps2 - aps1;
                diff.addValue(d);
                minDiff = min(minDiff, d);
                maxDiff = max(maxDiff, d);
                if (prevAps1 != null) {
                    avgDelta1.addValue(aps1 - prevAps1);
                    avgDelta2.addValue(aps2 - prevAps2);
                }
                prevAps1 = aps1;
                prevAps2 = aps2;
                cnt++;
            } catch (NumberFormatException ignore) {
            }
        }

        System.out.println("Diff = " + diff.getCurrentValue());
        System.out.println("Ref APS = " + avgAps1.getCurrentValue());
        System.out.println("Chel aps = " + avgAps2.getCurrentValue());
        System.out.println("Wrost change = " + minDiff);
        System.out.println("best change =" + maxDiff);
        System.out.println("Cnt = " + cnt);
    }

}
