# notes on leadership principles

as accessed https://amazon.jobs/content/en/our-workplace/leadership-principles
on `2023-12-30T02:59:15-05:00`.

### Customer Obsession
> Leaders start with the customer and work backwards. They work vigorously to
  earn and keep customer trust. Although leaders pay attention to
  competitors, they obsess over customers.

star:
* as a lead on an automation project, I implemented everything in pure python
  because the team was not python programmers, so time spent educating about
  dependency management would not have been time well spent, and it's a major
  python pain point which would have distracted us from spending time on:
  * auditing scripts
  * custom functionality not covered by azure provider
  * demos of how to use tools we deploy (Azure AI: chat, vector db, langchain, ...)
  * non-automated tasks (overcome temporary network infra limitations)
* as a lead on a jira ticket about implementing attachments for FHIR
  documents, I deduced from the use case that it is more widely applicable.
  the general purpose attachment mechanism which was designed and built
  in coordination with the team lead came into use not a month later.
  Insight into what teams were looking to do with our API was key
  (simplify the EPIC data model).
* In regulated environments, identifying the customer is key.
  * at ford, the difficulty with innovating on factory floor is deployment.
    you have to work closely with plant management partners, dynamic
    slightly modelled in test plant environment.
    on our project, the PMs were new to the manufacturing.
    * So while team manager chased some high-effort projects, we made
      dashboards for verifying deployments - something we knew was helping
      people on the other side. (First and foremost was a db log + UI for
      broker messages, so one could see if the inputs were ever sent.)

### Ownership
> Leaders are owners. They think long term and don’t sacrifice long-term value
  for short-term results. They act on behalf of the entire company, beyond
  just their own team. They never say “that’s not my job.”

star:
* as a backend engineer starting my career, I was looking for a second role,
  and I made sure I was doing some degree of frontend on a full stack team,
  and this ensured that I was able to take more ownership of the tasks.
  This indeed led to those kinds of opportunities, (e.g. the message log).
  It also increased my confidence in frontend tremendously (Angular & beyond).

### Invent and Simplify
> Leaders expect and require innovation and invention from their teams and
  always find ways to simplify. They are externally aware, look for new ideas
  from everywhere, and are not limited by “not invented here.” As we do new
  things, we accept that we may be misunderstood for long periods of time.

star:
* as a new engineer who isn't always challenged enough at work,
  I found there is a lot of value in following industry leaders (and
  celebrities) on social media (LinkedIn, Twitter (despite leavers)),
  in many different disciplines. This has led to learning about new startups,
  sharing expert knowledge about well-established technologies, and everything
  in between (Advent of Code, Andy Pavlo's DBDB, developments in open source
  projects, upcoming java features, spring libraries, ...).
* when on the FHIR project at 3m, I was onboarding most incoming engineers
  after my 1st year. One of them was competent but new to spring, and
  was fascinated with our usage of apache zookeeper. I coordinated a relevant
  task to be assigned and worked with him to debug, etc... encouraging the
  direction of experimentation and eventually guiding to a more abstract
  solution, which eventually was the basis of the improvements he added to
  our internal library.
* when tasked with building rest dependency for high throughput service,
  argued for using async stack. team lead disagreed, moved to sync stack.
  Continued to defend decision and exchange links to benchmarks and research.
  Eventually team lead came around, and we even built parts of the
  main high throughput service with async. The team was then ready,
  the team lead was convinced, and afterward, the codebase was improved.

### Are Right, A Lot
> Leaders are right a lot. They have strong judgment and good instincts. They
  seek diverse perspectives and work to disconfirm their beliefs.

*

### Learn and Be Curious
> Leaders are never done learning and always seek to improve themselves. They
  are curious about new possibilities and act to explore them.

*

### Hire and Develop the Best
> Leaders raise the performance bar with every hire and promotion. They
  recognize exceptional talent, and willingly move them throughout the
  organization. Leaders develop leaders and take seriously their role in
  coaching others. We work on behalf of our people to invent mechanisms for
  development like Career Choice.

* I really enjoy mentoring people.

star:
* I had to work with manager at ford to identify which team members were not
  meeting performance requirements. I was one of the earliest remote hires
  at the company, so it was a new phenomenon for them, whereas I started my
  corporate career in 2020 and so had all my experience with remote work.

### Insist on the Highest Standards
> Leaders have relentlessly high standards — many people may think these
  standards are unreasonably high. Leaders are continually raising the bar
  and drive their teams to deliver high quality products, services, and
  processes. Leaders ensure that defects do not get sent down the line and
  that problems are fixed so they stay fixed.

* being able to track your compromises

### Think Big
> Thinking small is a self-fulfilling prophecy. Leaders create and communicate
  a bold direction that inspires results. They think differently and look
  around corners for ways to serve customers.

### Bias for Action
> Speed matters in business. Many decisions and actions are reversible and do
  not need extensive study. We value calculated risk taking.

* prototyping at hackathons
* writing up designs and history simplifies reversing decisions
  * 'Architectural Decision Records' (or 'significant'-)
  * participated in this at 3m, taught to team at ford.

### Frugality
> Accomplish more with less. Constraints breed resourcefulness,
  self-sufficiency, and invention. There are no extra points for growing
  headcount, budget size, or fixed expense.

* wanted to use postgres for a service with a small dataset,
  as FHIR project used cassandra, but that is quite obnoxious for RDBMS tasks.
  From my standpoint, wanted to use "the right tool for the job".
  Team lead showed me AWS pricing for postgres, which made cleared up reasoning,
  to which I suggested trying to self-host the postgres on ec2
  (how hard could it be - well, maybe quite hard).
  In the end, realized that building for cost-effectiveness is its own
  exciting challenge, no less engineering-intensive than the others.
  Service was built with cassandra, energy spent investigating into
  spring async abstractions for cassandra instead.

### Earn Trust
> Leaders listen attentively, speak candidly, and treat others respectfully.
  They are vocally self-critical, even when doing so is awkward or
  embarrassing. Leaders do not believe their or their team’s body odor smells
  of perfume. They benchmark themselves and their teams against the best.

*

### Dive Deep
> Leaders operate at all levels, stay connected to the details, audit
  frequently, and are skeptical when metrics and anecdote differ. No task is
  beneath them.

* low level projects?
  * learning spring inside out?
  * ford proxy for custom tcp binary protocol
  * logging reverse proxy for OpenAI@bny

### Have Backbone; Disagree and Commit
> Leaders are obligated to respectfully challenge decisions when they
  disagree, even when doing so is uncomfortable or exhausting. Leaders have
  conviction and are tenacious. They do not compromise for the sake of social
  cohesion. Once a decision is determined, they commit wholly.

* I mean if I am made to change my engineering decisions, then I will work
  tirelessly to carry them out to their logical conclusion to test the hypothesis.
  Hoping, of course, to be proven right, but not necessarily.

### Deliver Results
> Leaders focus on the key inputs for their business and deliver them with the
  right quality and in a timely fashion. Despite setbacks, they rise to the
  occasion and never settle.

*

### Strive to be Earth’s Best Employer
> Leaders work every day to create a safer, more productive, higher
  performing, more diverse, and more just work environment. They lead with
  empathy, have fun at work, and make it easy for others to have fun. Leaders
  ask themselves: Are my fellow employees growing? Are they empowered? Are
  they ready for what’s next? Leaders have a vision for and commitment to
  their employees’ personal success, whether that be at Amazon or elsewhere.

* "workers don't leave a bad job, they leave a bad manager".
  if I want to keep my cool job, with all the cool co-workers there,
  then it is in my self-interest to maintain the work culture which attracted them,
  and improve it.

### Success and Scale Bring Broad Responsibility
> We started in a garage, but we’re not there anymore. We are big, we impact
  the world, and we are far from perfect. We must be humble and thoughtful
  about even the secondary effects of our actions. Our local communities,
  planet, and future generations need us to be better every day. We must
  begin each day with a determination to make better, do better, and be
  better for our customers, our employees, our partners, and the world at
  large. And we must end every day knowing we can do even more tomorrow.
  Leaders create more than they consume and always leave things better than
  how they found them.

* open source: the more you share the more you have.
