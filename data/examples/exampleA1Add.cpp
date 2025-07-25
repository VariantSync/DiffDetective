#ifdef Feature1
    int x = 5;
#ifdef Feature2
    for (int i = 0; i < 10; i++) {
    	x += 2;
    }
#else
    x += 10
#endif
#ifdef Feature4
    int y = 10;
#endif
    x *= x;
#endif
