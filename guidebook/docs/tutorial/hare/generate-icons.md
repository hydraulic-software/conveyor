* [x] Run `conveyor make rendered-icons`
* [x] Look in the `output` directory to find your new icons.

Basic, but functional.

* [x] Set the `app.icons` key to this:
  ```
  app {
    icons {
       label = XY
       gradient = blue
    }
  }
  ```
* [x] Rerun `conveyor make rendered-icons` and look at the results again.

The generated icons will be used automatically. 

{!tutorial/hare/learn-more-about-icons.md!}
