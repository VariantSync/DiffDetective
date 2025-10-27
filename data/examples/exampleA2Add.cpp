#ifdef Feature1
    int x = 5;
#ifdef Feature2
    for (int i = 0; i < 10; i++) {
    	x += 2;
    }
#endif
    x *= x;
    x += 1000;
#endif
