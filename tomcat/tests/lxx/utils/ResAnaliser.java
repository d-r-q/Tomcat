/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package lxx.utils;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ResAnaliser {

    private static final String str1 = "| [[lxx.Tomcat]] 3.45wsm || [[Author]] || Algorithm ||52,34 ||61,18 ||60,06 ||52,51 ||62,50 ||57,29 ||56,10 ||57,42 ||72,42 ||54,76 ||71,24 ||70,95 ||63,37 ||66,76 ||66,40 ||61,95 ||62,78 ||65,09 ||72,49 ||69,95 ||61,37 ||67,12 ||58,90 ||64,09 ||64,21 ||65,93 ||63,02 ||66,93 ||56,76 ||60,67 ||63,10 ||60,21 ||65,51 ||72,40 ||65,74 ||71,19 ||70,41 ||62,14 ||60,32 ||71,51 ||67,41 ||65,90 ||62,21 ||62,67 ||76,04 ||71,66 ||60,77 ||74,68 ||60,79 ||70,82 ||66,84 ||69,41 ||69,03 ||64,39 ||63,51 ||72,00 ||74,87 ||68,74 ||62,08 ||73,60 ||66,76 ||76,09 ||66,48 ||60,65 ||73,78 ||70,45 ||74,53 ||81,66 ||61,06 ||66,18 ||72,34 ||66,97 ||71,85 ||72,43 ||71,63 ||63,13 ||66,48 ||68,04 ||73,87 ||68,83 ||76,47 ||75,82 ||76,62 ||82,37 ||78,23 ||84,45 ||71,25 ||77,93 ||68,85 ||72,20 ||69,21 ||71,88 ||72,42 ||69,00 ||72,48 ||78,87 ||65,99 ||72,39 ||77,81 ||58,69 ||73,66 ||83,48 ||84,44 ||76,22 ||75,13 ||85,30 ||86,27 ||82,19 ||80,79 ||70,60 ||83,91 ||71,56 ||71,20 ||78,00 ||82,40 ||74,60 ||66,25 ||77,48 ||77,43 ||94,41 ||69,16 ||76,50 ||78,80 ||71,82 ||85,71 ||80,52 ||72,25 ||83,16 ||78,10 ||81,98 ||75,31 ||67,15 ||83,20 ||76,79 ||77,75 ||81,63 ||79,39 ||71,11 ||78,41 ||73,37 ||71,16 ||96,83 ||64,10 ||62,97 ||81,74 ||72,14 ||84,82 ||78,08 ||77,91 ||80,88 ||68,63 ||84,16 ||71,73 ||93,34 ||72,09 ||81,08 ||80,02 ||69,54 ||67,51 ||82,33 ||76,55 ||75,53 ||80,86 ||89,16 ||90,49 ||90,87 ||76,21 ||78,02 ||84,89 ||82,52 ||77,20 ||84,05 ||78,71 ||79,71 ||73,86 ||81,13 ||83,17 ||89,26 ||86,71 ||87,72 ||83,17 ||72,91 ||78,55 ||84,90 ||77,61 ||76,52 ||80,19 ||71,46 ||84,76 ||70,34 ||74,97 ||82,04 ||78,72 ||73,29 ||74,16 ||79,66 ||79,05 ||86,20 ||83,82 ||71,39 ||75,03 ||79,72 ||78,62 ||72,25 ||79,62 ||77,45 ||75,04 ||81,54 ||82,59 ||79,67 ||82,79 ||77,72 ||78,43 ||83,13 ||86,08 ||83,33 ||78,69 ||76,68 ||87,73 ||81,90 ||81,80 ||76,73 ||84,25 ||90,55 ||87,97 ||88,67 ||89,83 ||71,91 ||89,75 ||83,50 ||78,23 ||79,13 ||88,84 ||72,95 ||76,04 ||77,05 ||74,24 ||98,54 ||82,81 ||86,64 ||77,52 ||87,03 ||74,44 ||64,66 ||81,96 ||82,29 ||78,25 ||85,40 ||79,73 ||84,12 ||84,27 ||79,14 ||83,56 ||73,46 ||86,66 ||83,68 ||76,19 ||75,34 ||79,47 ||74,28 ||81,59 ||85,85 ||78,38 ||81,05 ||98,97 ||90,52 ||79,49 ||78,58 ||83,81 ||77,02 ||97,10 ||79,65 ||85,85 ||79,29 ||80,27 ||82,59 ||84,39 ||83,51 ||87,77 ||98,70 ||86,74 ||80,70 ||85,20 ||79,79 ||84,68 ||92,48 ||90,94 ||82,78 ||79,30 ||79,56 ||83,98 ||82,03 ||71,89 ||77,46 ||78,45 ||82,21 ||75,84 ||83,53 ||93,25 ||90,25 ||76,21 ||83,73 ||87,09 ||84,16 ||82,52 ||81,50 ||76,56 ||83,55 ||73,58 ||80,07 ||89,89 ||79,36 ||89,12 ||81,87 ||84,92 ||80,92 ||76,89 ||79,30 ||79,15 ||91,60 ||78,04 ||79,74 ||78,04 ||76,62 ||85,89 ||75,84 ||87,56 ||87,77 ||84,82 ||75,21 ||97,51 ||72,44 ||81,84 ||84,98 ||83,87 ||88,65 ||75,12 ||77,75 ||88,18 ||79,02 ||86,04 ||82,90 ||70,74 ||88,10 ||93,71 ||90,27 ||75,94 ||87,42 ||69,37 ||80,62 ||66,88 ||86,68 ||87,46 ||82,97 ||82,98 ||90,17 ||80,58 ||80,88 ||82,49 ||88,91 ||91,08 ||84,54 ||87,51 ||76,31 ||73,17 ||86,09 ||83,45 ||77,99 ||93,33 ||88,72 ||88,15 ||70,90 ||77,73 ||79,11 ||75,60 ||98,56 ||78,92 ||81,37 ||85,15 ||86,57 ||92,42 ||89,17 ||92,21 ||78,57 ||84,20 ||93,21 ||89,97 ||87,64 ||89,19 ||95,65 ||85,04 ||96,34 ||85,01 ||87,29 ||97,64 ||93,82 ||93,44 ||89,87 ||92,20 ||84,55 ||86,99 ||88,23 ||88,16 ||91,56 ||84,93 ||95,78 ||87,99 ||75,90 ||90,74 ||98,90 ||85,49 ||81,19 ||72,04 ||84,04 ||93,82 ||93,61 ||84,95 ||85,73 ||85,76 ||99,41 ||92,32 ||83,77 ||86,93 ||88,30 ||97,20 ||84,92 ||87,35 ||90,41 ||75,89 ||92,64 ||94,05 ||87,12 ||95,18 ||95,64 ||99,52 ||92,25 ||88,23 ||89,24 ||89,39 ||89,45 ||94,14 ||71,62 ||90,20 ||99,37 ||92,01 ||84,05 ||97,18 ||77,41 ||92,38 ||96,51 ||89,01 ||86,69 ||82,63 ||85,96 ||98,46 ||91,12 ||94,70 ||98,16 ||92,57 ||89,52 ||98,04 ||84,20 ||89,73 ||84,29 ||93,30 ||87,32 ||95,64 ||96,49 ||97,77 ||98,07 ||84,15 ||92,85 ||97,46 ||81,85 ||97,57 ||93,55 ||77,64 ||87,78 ||96,14 ||90,25 ||88,11 ||88,19 ||81,41 ||94,50 ||82,84 ||98,11 ||90,13 ||99,73 ||96,30 ||85,77 ||95,68 ||92,25 ||89,67 ||97,15 ||98,46 ||97,15 ||98,63 ||99,82 ||91,22 ||88,14 ||92,03 ||88,69 ||94,77 ||82,13 ||98,38 ||98,49 ||99,24 ||99,61 ||97,35 ||98,81 ||91,76 ||90,31 ||99,54 ||96,88 ||88,37 ||98,82 ||97,56 ||99,53 ||96,23 ||82,50 ||89,80 ||85,37 ||92,24 ||88,21 ||97,02 ||86,51 ||80,28 ||97,84 ||97,08 ||84,05 ||87,08 ||88,26 ||92,19 ||97,10 ||95,83 ||95,75 ||92,96 ||85,98 ||99,23 ||94,56 ||88,73 ||98,98 ||99,56 ||96,20 ||87,83 ||95,31 ||98,51 ||96,69 ||96,46 ||96,99 ||97,30 ||97,62 ||86,33 ||95,40 ||93,74 ||97,64 ||87,69 ||94,80 ||97,99 ||88,94 ||90,64 ||94,99 ||97,47 ||95,85 ||99,84 ||80,46 ||98,65 ||94,63 ||91,75 ||97,59 ||97,58 ||96,19 ||96,58 ||98,79 ||94,70 ||92,56 ||92,84 ||91,12 ||97,24 ||87,27 ||99,49 ||99,08 ||88,62 ||89,80 ||88,36 ||98,20 ||96,33 ||99,11 ||95,56 ||93,51 ||96,37 ||96,54 ||97,83 ||94,97 ||92,71 ||99,49 ||96,79 ||90,63 ||96,08 ||100,00 ||88,69 ||99,29 ||98,79 ||92,49 ||98,92 ||94,45 ||97,73 ||91,85 ||99,07 ||93,09 ||94,52 ||91,61 ||85,35 ||98,40 ||94,74 ||99,75 ||94,75 ||99,35 ||99,43 ||93,65 ||95,89 ||95,45 ||97,54 ||98,24 ||99,69 ||99,80 ||91,91 ||99,52 ||98,86 ||88,28 ||99,20 ||98,20 ||98,61 ||97,04 ||97,80 ||93,95 ||95,55 ||94,36 ||99,45 ||98,39 ||96,27 ||94,26 ||88,43 ||99,68 ||91,36 ||98,28 ||99,82 ||99,40 ||94,23 ||95,11 ||97,77 ||93,32 ||96,30 ||97,84 ||99,29 ||98,55 ||99,97 ||93,69 ||98,26 ||98,02 ||98,03 ||96,22 ||95,95 ||98,99 ||90,59 ||94,94 ||99,56 ||94,10 ||96,83 ||98,58 ||99,58 ||91,22 ||94,18 ||92,45 ||99,62 ||98,30 ||99,73 ||90,45 ||98,74 ||90,24 ||93,99 ||90,19 ||98,58 ||96,50 ||98,69 ||95,79 ||96,06 ||93,02 ||99,32 ||99,51 ||91,73 ||99,63 ||99,68 ||96,76 ||87,79 ||99,61 ||99,08 ||85,89 ||99,12 ||99,82 ||96,87 ||98,15 ||98,66 ||91,31 ||97,78 ||99,26 ||98,52 ||99,89 ||93,59 ||88,15 ||95,61 ||97,13 ||93,14 ||99,99 ||98,74 ||92,98 ||99,23 ||95,48 ||91,12 ||95,69 ||92,49 ||99,31 ||88,13 ||99,36 ||97,75 ||99,00 ||94,83 ||97,40 ||96,09 ||98,69 ||98,61 ||99,90 ||99,51 ||84,21 ||99,73 ||95,79 ||98,00 ||99,77 ||98,18 ||98,78 ||96,64 ||98,31 ||99,58 ||99,67 ||96,94 ||97,33 ||99,11 ||99,18 ||87,50 ||98,32 ||97,23 ||98,07 ||95,38 ||98,54 ||99,11 ||94,58 ||99,78 ||98,21 ||99,67 ||96,32 ||97,45 ||99,75 ||99,66 ||99,67 ||98,87 ||99,36 ||96,81 ||99,49 ||99,33 ||99,60 ||99,98 ||97,86 ||99,90 ||95,92 ||97,59 ||96,56 ||94,91 ||99,34 ||99,94 ||99,50 ||99,64 ||99,03 ||99,35 ||99,95 ||99,88 ||98,99 ||95,03 ||99,92 ||98,53 ||99,10 ||99,92 ||94,82 ||83,91 ||99,99 ||100,00 ||98,24 ||99,86 ||99,68 ||100,00 ||99,91 ||98,94 ||99,50 ||99,53 ||99,20 ||100,00 ||100,00 ||99,82 ||100,00 ||99,97 ||99,94 ||98,08 ||100,00 ||100,00 ||99,80 ||99,95 ||99,88 ||99,49 ||100,00 ||99,36 ||99,98 ||99,24 ||99,84 ||99,11 ||99,97 ||99,92 ||99,80 ||99,98 ||99,94 ||98,91 ||99,95 ||100,00 ||99,73 ||100,00 ||100,00 ||100,00 ||100,00 ||'''86,88 (88,40)''' ||'''86,88 (88,40)''' ||4 seasons";
    private static final String str2 = "| [[lxx.Tomcat]] 3.47gt2 || [[Author]] || Algorithm ||48,76 ||69,61 ||56,91 ||56,83 ||66,50 ||56,07 ||57,30 ||66,28 ||65,37 ||49,04 ||67,09 ||65,83 ||68,34 ||67,84 ||64,59 ||57,67 ||65,74 ||60,43 ||69,59 ||61,45 ||67,48 ||65,79 ||71,03 ||66,80 ||72,79 ||63,37 ||57,07 ||68,85 ||65,61 ||65,71 ||62,24 ||56,20 ||54,53 ||59,90 ||76,15 ||64,16 ||69,07 ||67,44 ||65,74 ||70,79 ||63,13 ||69,78 ||65,75 ||58,48 ||75,02 ||63,58 ||49,46 ||66,81 ||58,54 ||76,99 ||73,65 ||61,10 ||73,95 ||68,00 ||72,48 ||76,42 ||65,01 ||80,97 ||69,66 ||75,81 ||66,14 ||68,94 ||69,29 ||59,33 ||61,94 ||66,64 ||64,94 ||82,64 ||65,66 ||70,22 ||69,42 ||72,61 ||61,79 ||73,05 ||76,10 ||62,16 ||73,80 ||74,72 ||71,84 ||74,35 ||78,31 ||82,14 ||73,84 ||84,30 ||80,38 ||81,29 ||76,42 ||76,15 ||66,51 ||71,15 ||73,38 ||63,64 ||77,64 ||71,93 ||69,79 ||80,81 ||72,74 ||67,17 ||70,31 ||68,22 ||71,88 ||87,45 ||80,17 ||76,62 ||71,08 ||86,95 ||85,17 ||78,40 ||79,45 ||64,22 ||86,78 ||68,65 ||78,42 ||77,63 ||78,66 ||75,70 ||60,56 ||75,08 ||81,05 ||91,39 ||69,50 ||66,26 ||78,75 ||71,15 ||85,34 ||84,26 ||72,81 ||85,36 ||70,83 ||81,00 ||78,16 ||78,12 ||84,16 ||77,98 ||78,72 ||74,53 ||80,12 ||63,36 ||67,57 ||72,53 ||70,01 ||97,02 ||60,31 ||71,21 ||75,47 ||67,01 ||83,98 ||75,85 ||79,48 ||87,22 ||73,19 ||89,21 ||73,64 ||90,54 ||75,69 ||79,73 ||72,67 ||79,95 ||58,19 ||80,14 ||71,44 ||73,04 ||82,69 ||88,31 ||90,52 ||89,15 ||78,36 ||73,52 ||87,78 ||75,75 ||69,06 ||83,94 ||75,87 ||81,36 ||78,63 ||78,79 ||87,07 ||92,60 ||91,08 ||91,88 ||84,98 ||65,17 ||79,29 ||88,91 ||72,97 ||80,63 ||78,27 ||70,43 ||87,54 ||71,66 ||81,55 ||79,30 ||80,97 ||76,14 ||75,45 ||78,20 ||82,75 ||82,90 ||84,97 ||66,17 ||77,82 ||80,46 ||NaN ||73,97 ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||75,35 ||81,69 ||81,98 ||89,72 ||74,37 ||75,51 ||78,95 ||75,40 ||83,03 ||79,08 ||88,07 ||86,75 ||85,91 ||81,38 ||96,87 ||77,34 ||82,10 ||79,51 ||86,17 ||85,56 ||80,23 ||81,10 ||77,47 ||85,51 ||82,00 ||85,22 ||70,34 ||84,49 ||95,87 ||91,71 ||78,57 ||90,26 ||69,55 ||83,35 ||68,02 ||84,99 ||89,45 ||80,78 ||91,15 ||91,28 ||79,73 ||79,82 ||89,34 ||88,13 ||88,40 ||84,83 ||87,69 ||83,12 ||74,74 ||86,06 ||80,13 ||78,58 ||94,35 ||89,05 ||84,30 ||72,34 ||74,20 ||82,67 ||77,43 ||99,09 ||89,01 ||80,16 ||84,32 ||79,09 ||92,92 ||91,49 ||91,14 ||77,15 ||89,72 ||92,79 ||88,89 ||90,01 ||88,10 ||95,07 ||86,13 ||96,63 ||85,58 ||88,62 ||97,72 ||95,07 ||92,08 ||90,95 ||96,41 ||85,40 ||86,03 ||86,95 ||83,27 ||91,70 ||87,13 ||93,29 ||91,47 ||74,48 ||95,91 ||98,54 ||84,20 ||80,09 ||72,42 ||86,91 ||92,65 ||93,42 ||84,40 ||89,32 ||89,55 ||99,36 ||92,80 ||84,39 ||88,94 ||87,30 ||98,96 ||84,23 ||86,03 ||91,41 ||68,89 ||93,75 ||93,01 ||87,10 ||97,10 ||95,23 ||99,35 ||92,08 ||89,46 ||87,61 ||88,59 ||86,74 ||93,93 ||73,50 ||89,19 ||99,43 ||92,34 ||90,16 ||98,17 ||79,08 ||93,71 ||96,83 ||88,19 ||87,67 ||83,35 ||87,58 ||98,23 ||92,40 ||97,42 ||99,18 ||89,06 ||90,52 ||97,93 ||87,75 ||91,94 ||85,35 ||93,35 ||90,29 ||96,57 ||95,93 ||98,19 ||97,87 ||81,39 ||92,95 ||99,01 ||80,65 ||98,03 ||91,74 ||79,14 ||92,16 ||96,37 ||89,32 ||90,39 ||92,60 ||77,27 ||95,03 ||88,66 ||96,82 ||93,67 ||100,00 ||96,80 ||88,30 ||95,28 ||92,94 ||91,61 ||98,05 ||97,18 ||94,58 ||98,79 ||100,00 ||91,26 ||88,04 ||93,06 ||92,46 ||96,24 ||81,14 ||98,90 ||98,34 ||98,45 ||95,36 ||97,58 ||99,01 ||91,78 ||88,69 ||99,38 ||97,49 ||86,66 ||98,46 ||97,56 ||99,97 ||97,00 ||85,23 ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||NaN ||98,37 ||97,57 ||97,49 ||94,04 ||95,42 ||95,04 ||99,63 ||97,48 ||96,56 ||97,08 ||85,71 ||99,75 ||92,00 ||98,15 ||99,87 ||99,48 ||93,34 ||97,69 ||99,38 ||93,78 ||96,30 ||99,95 ||99,90 ||99,30 ||98,95 ||94,23 ||98,72 ||98,28 ||96,81 ||94,56 ||94,59 ||99,01 ||89,20 ||93,04 ||99,52 ||93,91 ||97,59 ||96,62 ||99,23 ||93,39 ||95,10 ||92,83 ||99,68 ||98,42 ||99,67 ||90,67 ||98,74 ||87,61 ||94,01 ||89,50 ||98,55 ||92,59 ||98,49 ||92,01 ||95,70 ||94,37 ||99,83 ||99,20 ||89,71 ||99,89 ||99,78 ||96,13 ||88,16 ||99,35 ||98,70 ||87,20 ||98,61 ||99,80 ||96,47 ||98,94 ||97,67 ||88,31 ||98,22 ||98,53 ||100,00 ||99,87 ||96,21 ||88,58 ||96,15 ||97,68 ||93,72 ||99,98 ||97,63 ||91,74 ||100,00 ||93,11 ||94,60 ||94,81 ||91,33 ||99,17 ||87,21 ||99,24 ||97,28 ||98,96 ||95,50 ||95,82 ||95,85 ||98,90 ||99,02 ||99,92 ||99,26 ||83,43 ||99,66 ||94,40 ||98,37 ||99,33 ||99,01 ||98,37 ||97,70 ||99,08 ||99,62 ||99,73 ||98,70 ||97,87 ||99,01 ||99,19 ||99,94 ||99,09 ||97,69 ||98,48 ||96,67 ||99,51 ||99,18 ||95,48 ||99,69 ||98,27 ||99,19 ||95,61 ||97,05 ||99,58 ||99,70 ||99,89 ||98,71 ||99,07 ||97,25 ||99,65 ||99,25 ||99,46 ||99,96 ||96,19 ||99,81 ||94,39 ||97,28 ||95,85 ||94,03 ||99,34 ||100,00 ||99,54 ||99,82 ||99,31 ||100,00 ||100,00 ||100,00 ||99,61 ||95,59 ||100,00 ||99,14 ||97,91 ||99,69 ||96,49 ||83,25 ||100,00 ||100,00 ||98,12 ||98,00 ||99,74 ||100,00 ||100,00 ||98,88 ||99,70 ||99,73 ||99,48 ||100,00 ||100,00 ||99,77 ||100,00 ||99,86 ||99,93 ||98,33 ||100,00 ||100,00 ||100,00 ||100,00 ||99,50 ||99,79 ||99,84 ||100,00 ||100,00 ||98,89 ||99,93 ||98,74 ||100,00 ||99,98 ||99,84 ||100,00 ||99,93 ||98,67 ||99,98 ||99,93 ||99,97 ||100,00 ||100,00 ||100,00 ||100,00 ||'''86,45 (80,42)''' ||'''86,45 (80,42)''' ||4 seasons";

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
